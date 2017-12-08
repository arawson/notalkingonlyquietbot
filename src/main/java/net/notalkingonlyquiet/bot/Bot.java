package net.notalkingonlyquiet.bot;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.notalkingonlyquiet.bot.audio.GuildMusicManager;
import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.googlesearch.YouTubeSearcher;
import org.apache.http.client.config.RequestConfig;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.IVoiceState;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.cache.LongMap;

/**
 *
 * @author arawson
 */
public final class Bot {

    private final IDiscordClient client;
    private final String prefix;
    private final int maxServers;
    
    //associate servers to audio channels
    private final Map<IGuild, IChannel> lastChannel = new HashMap<>();
    private final Map<IGuild, GuildMusicManager> musicManagers = new HashMap<>();
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<String, Command> commands = new HashMap<>();
    
    private final MemeManager memeManager;

    private final ScheduledThreadPoolExecutor busExecutor;
    private final EventBus eventBus;
    private String playing;
    private final YouTubeSearcher youTubeSearcher;

    private boolean dead = false;

    public Bot(IDiscordClient client, Config config) throws IOException {
        busExecutor = new ScheduledThreadPoolExecutor(config.performance.threads);
        eventBus = new AsyncEventBus(busExecutor);

        prefix = MoreObjects.firstNonNull(config.bot.prefix, "!");
        maxServers = config.performance.servers;
        playing = config.login.playing;
        this.client = client;

        youTubeSearcher = new YouTubeSearcher(config.google);
        memeManager = new MemeManager(config.memes);

        Arrays.asList(
                new PlayCommand(this),
                new SkipCommand(this),
                new VolumeCommand(this),
                new MemeCommand(this),
                new AddMemeCommand(this)
        ).stream().forEach(
                cmd -> {
                    commands.put(cmd.getBase(), cmd);
                });

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        playerManager.setHttpRequestConfigurator(
                (cfg) -> RequestConfig.copy(cfg)
                        .setConnectTimeout(config.performance.timeout)
                        .build());

        client.getDispatcher().registerListener(this);
    }

    public boolean isDead() {
        return dead;
    }

    public void forceShutdown() {
        //TODO: what cleanup on forced shutdown?
        memeManager.deinit();
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) {
        LogUtil.logInfo("Connection ready.");
        onGuildCreateOrJoin(null);
		client.changePlayingText(playing);
    }

    @EventSubscriber
    public void onGuildCreateOrJoin(GuildCreateEvent e) {

        LogUtil.logInfo("Checking server connection limit...");
        try {
            if (client.getGuilds().size() > maxServers) {
                if (e == null) {
                    //refuse to start if we have too many guilds at startup
                    LogUtil.logError("Connected to too many servers on startup. ABORT.");
                    dead = true;
                } else {
                    //leave unexpected guilds o keep server costs low
					e.getGuild().leave();
                }
            } else {
                LogUtil.logInfo("Under connection limit, continuing...");
            }
        } catch (DiscordException | RateLimitException ex) {
            LogUtil.logError("Connected to too many servers, but could not leave latest server. ABORT.");
            dead = true;
        }
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent event) {
        IMessage message = event.getMessage();
        IChannel channel = message.getChannel();
        IUser user = message.getAuthor();

        if (user.isBot()) {
            return;
        }

        LogUtil.logInfo(message.getGuild().getName() + ":" + channel.getName() + ":" + user.getName() + ": " + message.getContent());
        String[] split = message.getContent().split(" ");

        if (split.length >= 1 && split[0].startsWith(prefix)) {
            String command = split[0].replaceFirst(prefix, "");
            String[] args = split.length >= 2
                    ? Arrays.copyOfRange(split, 1, split.length)
                    : new String[0];
            
            internalCommand(command, args, channel, user);
        }
    }
    
    synchronized void internalCommand(String command, String[] args, IChannel channel, IUser user) {
        //TODO: decouple using event bus

        Command c = commands.get(command);
        if (c == null) {
            FireAndForget.sendMessage(channel, "I'm sorry " + user.getName() + ". I'm afraid I can't do that.");
        } else {
            try {
                c.execute(args, channel, user);
            } catch (RateLimitException | DiscordException | MissingPermissionsException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    GuildMusicManager getGuildMusicManager(IGuild guild) {
        GuildMusicManager mm = musicManagers.get(guild);
        if (mm == null) {
            mm = new GuildMusicManager(playerManager, eventBus, this);
            musicManagers.put(guild, mm);
        }

        guild.getAudioManager().setAudioProvider(mm.getAudioProvider());
        
        return mm;
    }
	
	public IVoiceChannel getLikelyUserVoiceChannel(IUser user) {
		LongMap<IVoiceState> vs = user.getVoiceStates();
		IVoiceChannel voice = null;
		for (IVoiceState v : vs.values()) {
			IVoiceChannel ch = v.getChannel();
			if (ch != null && client.getGuilds().contains(ch.getGuild())) {
				//this is the channel we should connect to
				voice = ch;
				break;
			}
		}
		
		return voice;
	}

    boolean joinUsersAudioChannel(IChannel channel, IUser user) {
        boolean result = false;
        Preconditions.checkArgument(!user.isBot(), "I don't answer to bots like you, " + user.getName() + ".");
		
		IVoiceChannel voice = getLikelyUserVoiceChannel(user);

		if (voice == null) {
			FireAndForget.sendMessage(channel, "You aren't in any voice channels of the servers I am in.");
		} else if (!voice.getModifiedPermissions(client.getOurUser()).contains(Permissions.VOICE_CONNECT)) {
			FireAndForget.sendMessage(channel, "Can't join " + voice.getName() + " without the voice permission!");
		} else if (voice.getUserLimit() != 0 && voice.getConnectedUsers().size() >= voice.getUserLimit()) {
			FireAndForget.sendMessage(channel, "Can't join " + voice.getName() + ". It is already full.");
		} else {
			FireAndForget.joinVoice(voice);

			getGuildMusicManager(channel.getGuild()).setCurrentVoiceChannel(voice);
			FireAndForget.sendMessage(channel, "Connecting to " + voice.getName() + ".");
			result = true;
		}
        
        return result;
    }

    YouTubeSearcher getYouTubeSearcher() {
        return youTubeSearcher;
    }
    
    AudioPlayerManager getAudioPlayerManager() {
        return playerManager;
    }

    MemeManager getMemeManager() {
        return memeManager;
    }
}
