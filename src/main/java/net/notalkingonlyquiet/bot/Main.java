package net.notalkingonlyquiet.bot;

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
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.sound.sampled.UnsupportedAudioFileException;
import net.notalkingonlyquiet.bot.config.Config;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.audio.IAudioManager;
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
import sx.blah.discord.util.audio.AudioPlayer;

public class Main {

    public static final String PREFIX = "!";

    private final Config config;
    private final IDiscordClient client;
    
    //associate servers to audio channels
    private final Map<IGuild, IChannel> lastChannel = new HashMap<>();
    
    //lavaplayer data
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();
    private final Map<Long, GuildMusicManager> musicManagers = new HashMap<>();

    public Main(Config config) throws DiscordException, RateLimitException {
        this.config = config;

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
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) {
        LogUtil.logInfo("Connection ready.");
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
                case "join":
                    handled = true;
                    lastChannel.put(guild, channel);
                    join(channel, user);
                    break;
                    
                case "playurl":
                    handled = playUrl(channel, args);
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
                channel.sendMessage("Connected to **" + voice.getName() + "**.");
            }
        }
    }
    
    private boolean playUrl(IChannel channel, String[] args) throws MissingPermissionsException, RateLimitException, DiscordException {
        if (args.length == 0) {
            channel.sendMessage("You must give me a URL as the first argument to that command.");
            return false;
        }
        
        try {
            URL u = new URL(args[0]);
            GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
            
            playerManager.loadItemOrdered(musicManager, u.toString(), new AudioLoadResultHandler() {
                @Override
                public void trackLoaded(AudioTrack track) {
                    try {
                        channel.sendMessage("Adding to queue " + track.getInfo().title);
                    } catch (MissingPermissionsException | DiscordException | RateLimitException ex) {
                        LogUtil.logError(ex.getLocalizedMessage());
                    }
                    
                    play(channel.getGuild(), musicManager, track);
                }

                @Override
                public void playlistLoaded(AudioPlaylist playlist) {
                    AudioTrack firstTrack = playlist.getSelectedTrack();
                    if (firstTrack == null) {
                        firstTrack = playlist.getTracks().get(0);
                    }
                    try {
                        channel.sendMessage("Adding to queue " +
                                firstTrack.getInfo().title +
                                " (first track of playlist " + playlist.getName() + ")");
                    } catch (MissingPermissionsException | DiscordException | RateLimitException ex) {
                        LogUtil.logError(ex.getLocalizedMessage());
                    }
                    play(channel.getGuild(), musicManager, firstTrack);
                }

                @Override
                public void noMatches() {
                    try {
                        channel.sendMessage("Could not find anything at " + u.toString());
                    } catch (MissingPermissionsException | DiscordException | RateLimitException ex) {
                        LogUtil.logError(ex.getLocalizedMessage());
                    }
                }

                @Override
                public void loadFailed(FriendlyException fe) {
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
        connectToFirstVoiceChannel(guild.getAudioManager());
        musicManager.scheduler.queue(track);
    }
    
    private synchronized GuildMusicManager getGuildAudioPlayer(IGuild guild) {
        long guildId = Long.parseLong(guild.getID());
        GuildMusicManager musicManager = musicManagers.get(guildId);
        
        if (musicManager == null) {
            musicManager = new GuildMusicManager(playerManager);
            musicManagers.put(guildId, musicManager);
        }
        
        //TODO: needlesss object construction all the way down :(
        guild.getAudioManager().setAudioProvider(musicManager.getAudioProvider());
        
        return musicManager;
    }
    
    private boolean skipTrack(IChannel channel, String[] args) throws MissingPermissionsException, RateLimitException, DiscordException {
        GuildMusicManager musicManager = getGuildAudioPlayer(channel.getGuild());
        musicManager.scheduler.nextTrack();
        channel.sendMessage("Skipped to next track.");
        return true;
    }

    private void connectToFirstVoiceChannel(IAudioManager audioManager) {
        for (IVoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
            if (voiceChannel.isConnected()) {
                return;
            }
        }
        
        //TODO: test this against multiple voice channels
        for (IVoiceChannel voiceChannel : audioManager.getGuild().getVoiceChannels()) {
            try {
                voiceChannel.join();
            } catch (MissingPermissionsException e) {
                LogUtil.logError("Cannot enter voice channel " + voiceChannel.getName() + " " + e);
            }
        }
    }
}
