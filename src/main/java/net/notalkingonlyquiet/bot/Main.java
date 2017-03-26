package net.notalkingonlyquiet.bot;

import net.notalkingonlyquiet.bot.audio.GuildMusicManager;
import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.moandjiezana.toml.Toml;
import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.File;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import net.notalkingonlyquiet.bot.config.Config;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.audio.IAudioManager;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

public class Main {

    public static final String PREFIX = "!";

    private final Config config;
    private final IDiscordClient client;

    //associate servers to audio channels
    private final Map<IGuild, IChannel> lastChannel = new HashMap<>();

    //lavaplayer data
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    private final ScheduledThreadPoolExecutor executor;
    private final EventBus eventBus;

    public Main(Config config) throws DiscordException, RateLimitException {
        this.config = config;
        //TODO: proper exception handlers
        executor = new ScheduledThreadPoolExecutor(config.performance.threads);
        eventBus = new AsyncEventBus(executor);

        LogUtil.logInfo("Attempting to connect to Discord...");

        ClientBuilder cb = new ClientBuilder()
                .withToken(config.login.token);
        client = cb.build();

        client.getDispatcher().registerListener(this);

        client.login();

        LogUtil.logInfo("Login Successful...");

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    void shutdown() throws DiscordException {
        LogUtil.logInfo("Closing connection...");
        client.logout();
    }

    /**
     * @param args the command line arguments
     */
    public static void main(String[] args) throws DiscordException, RateLimitException, IOException {
        LogUtil.initLogging();

        Toml toml = new Toml().read(new File("./config.toml"));
        Config config = toml.to(Config.class);

        Main main = new Main(config);
        
        System.in.read();
        
        main.exit();
    }

    @EventSubscriber
    public void onGuildCreateOrJoin(GuildCreateEvent e) {
        //performance check, don't connect to too many servers
        abortIfTooManyGuilds(e);
    }

    private void abortIfTooManyGuilds(GuildCreateEvent e) {
        LogUtil.logInfo("Checking server connection limit...");
        try {
            if (client.getGuilds().size() > config.performance.servers) {
                if (e == null) {
                    //refuse to start if we have too many guilds at startup
                    LogUtil.logError("Connected to too many servers on startup. ABORT.");
                    System.exit(1);
                } else {
                    //leave unexpected guilds to keep server costs low
                    e.getGuild().leaveGuild();
                }
            } else {
                LogUtil.logInfo("Under connection limit, continuing...");
            }
        } catch (DiscordException | RateLimitException ex) {
            LogUtil.logError("Connected to too many servers, but could not leave latest server. ABORT.");
            System.exit(1);
        }
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) {
        LogUtil.logInfo("Connection ready.");
        abortIfTooManyGuilds(null);
        client.changeStatus(Status.game(config.login.playing));
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent event) throws
            MissingPermissionsException, MissingPermissionsException, RateLimitException, DiscordException {
        IMessage message = event.getMessage();
        IUser user = message.getAuthor();

        if (user.isBot()) {
            return;
        }

        IChannel channel = message.getChannel();
        IGuild guild = message.getGuild(); //guild is server

        LogUtil.logInfo(guild.getName() + ":" + channel.getName() + ":"
                + user.getName() + ": " + message.getContent());

        String[] split = message.getContent().split(" ");

        if (split.length >= 1 && split[0].startsWith(PREFIX)) {
            String command = split[0].replaceFirst(PREFIX, "");
            String[] args = split.length >= 2
                    ? Arrays.copyOfRange(split, 1, split.length)
                    : new String[0];

            boolean handled = false;
            switch (command) {
                case "play":
                    lastChannel.put(guild, channel);
                    join(channel, user);
                    handled = playUrl(channel, user, args);
                    break;

                case "skip":
                    handled = skipTrack(channel, args);
                    break;
            }

            if (!handled) {
                LogUtil.logInfo("Unknown command or error processing: " + message.toString());
            }
        }
    }

    private void join(IChannel channel, IUser user) throws MissingPermissionsException, RateLimitException, DiscordException {
        if (user.getConnectedVoiceChannels().size() < 1) {
            LogUtil.logInfo("No channel to connect to for " + user.getName());
            channel.sendMessage("You aren't in a voice channel.");
        } else {
            //TODO: user's channel 0 might not be the expected channel to join
            IVoiceChannel voice = user.getConnectedVoiceChannels().get(0);
            if (!voice.getModifiedPermissions(client.getOurUser()).contains(Permissions.VOICE_CONNECT)) {
                channel.sendMessage("I can't join that voice channel.");
            } else if (voice.getUserLimit() != 0 && voice.getConnectedUsers().size() >= voice.getUserLimit()) {
                channel.sendMessage("That room is full!");
            } else {
                voice.join();
                getGuildAudioPlayer(channel.getGuild()).setCurrentVoiceChannel(voice);
                channel.sendMessage("Connected to **" + voice.getName() + "**.");
            }
        }
    }

    private boolean playUrl(IChannel channel, IUser user, String[] args) throws MissingPermissionsException, RateLimitException, DiscordException {
        LogUtil.logInfo("playUrl command");
        if (args.length == 0) {
            channel.sendMessage("You must give me a URL as the first argument to that command.");
            return false;
        }

        try {
            URL u = new URL(args[0]);
            LogUtil.logInfo("load music manager");
            GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());

            playerManager.loadItemOrdered(musicManager, u.toString(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    LogUtil.logInfo("track loaded");
                    try {
                        channel.sendMessage("Adding to queue " + track.getInfo().title);
                    } catch (MissingPermissionsException | DiscordException | RateLimitException ex) {
                        LogUtil.logError(ex.getLocalizedMessage());
                    }

                    musicManager.userQueue(channel, user, track);
//                    play(channel.getGuild(), musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    LogUtil.logInfo("playlist loaded");
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }
                    try {
                        channel.sendMessage("Adding to queue "
                                + firstTrack.getInfo().title
                                + " (first track of playlist " + playlist.getName() + ")");
                    } catch (MissingPermissionsException | DiscordException | RateLimitException ex) {
                        LogUtil.logError(ex.getLocalizedMessage());
                    }
                    
                    musicManager.userQueue(channel, user, firstTrack);
                }

                @Override
                public void noMatches() {
                    LogUtil.logInfo("no match");
                    try {
                        channel.sendMessage("Could not find anything at " + u.toString());
                    } catch (MissingPermissionsException | DiscordException | RateLimitException ex) {
                        LogUtil.logError(ex.getLocalizedMessage());
                    }
                }

                @Override
                public void loadFailed(FriendlyException fe) {
                    LogUtil.logInfo("load failed");
                    try {
                        channel.sendMessage("Could not play " + u.toString());
                    } catch (MissingPermissionsException | DiscordException | RateLimitException ex) {
                        LogUtil.logError(ex.getLocalizedMessage());
                    }
                }
            });

            //setTrackTitle(getPlayer(channel.getGuild()).queue(u), u.getFile());
        } catch (MalformedURLException ex) {
            LogUtil.logError(ex.toString());
            channel.sendMessage("You must give me a _valid_ URL as the first argument to that command.");
            return false;
        } catch (IOException ex) {
            LogUtil.logError(ex.toString());
            channel.sendMessage("Error playing URL: " + ex.getMessage());
        }

        return true;
    }

    private void play(IGuild guild, GuildMusicManager musicManager, AudioTrack track) {
        LogUtil.logInfo("play subroutine");
//        connectToFirstVoiceChannel(guild.getAudioManager());
//        musicManager.queue(track);
//        musicManager.userQueue(channel, user);
    }

    private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild) {
        long guildId = Long.parseLong(guild.getID());
        GuildMusicManager musicManager = musicManagers.get(guildId);

        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager, eventBus);
            musicManagers.put(guildId, musicManager);
        }

        //TODO: needlesss object construction all the way down :(
        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());

        return musicManager;
    }

    private boolean skipTrack(IChannel channel, String[] args) throws MissingPermissionsException, RateLimitException, DiscordException {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.nextTrack();
        channel.sendMessage("Skipped to next track.");
        return true;
    }

    private void connectToFirstVoiceChannel(IAudioManager audioManager) {
        LogUtil.logInfo("connect to first voice channel");
        for (IVoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
            if (voiceChannel.isConnected()) {
                return;
            }
        }

        LogUtil.logInfo("connecting to arbitrary voice channel");
        //TODO: test this against multiple voice channels
        for (IVoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
            try {
                voiceChannel.join();
            } catch (MissingPermissionsException e) {
                LogUtil.logError("Cannot enter voice channel " + voiceChannel.getName() + " " + e);
            }
        }
    }
    
    private void exit() throws DiscordException {
        client.logout();
    }
}
