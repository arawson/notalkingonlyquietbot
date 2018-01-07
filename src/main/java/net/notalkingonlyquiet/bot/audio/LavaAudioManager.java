package net.notalkingonlyquiet.bot.audio;

import com.google.common.base.Preconditions;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import net.notalkingonlyquiet.bot.util.LogUtil;
import org.apache.http.client.config.RequestConfig;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.audio.AudioEncodingType;
import sx.blah.discord.handle.audio.IAudioProvider;
import sx.blah.discord.handle.obj.*;
import sx.blah.discord.util.cache.LongMap;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;

@Component
@Scope(value = "singleton")
public class LavaAudioManager {
    private final Map<IGuild, GuildMusicManager> musicManagers = new HashMap<>();
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    @Autowired
    public LavaAudioManager(Config config) {
        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);

        playerManager.setHttpRequestConfigurator(
                (cfg) -> RequestConfig.copy(cfg)
                        .setConnectTimeout(config.performance.timeout)
                        .build());
    }

    GuildMusicManager getGuildMusicManager(IGuild guild) {
        GuildMusicManager mm = musicManagers.get(guild);
        if (mm == null) {
            mm = new GuildMusicManager(playerManager);
            musicManagers.put(guild, mm);
        }

        guild.getAudioManager().setAudioProvider(mm.getAudioProvider());

        return mm;
    }

    AudioPlayerManager getAudioPlayerManager() {
        return playerManager;
    }

    boolean joinUsersAudioChannel(IChannel channel, IUser user) {
        boolean result = false;
        Preconditions.checkArgument(!user.isBot(), "I don't answer to bots like you, " + user.getName() + ".");

        IVoiceChannel voice = getLikelyUserVoiceChannel(user);

        if (voice == null) {
            FireAndForget.sendMessage(channel, "You aren't in any voice channels of the servers I am in.");
        } else if (!voice.getModifiedPermissions(channel.getClient().getOurUser()).contains(Permissions.VOICE_CONNECT)) {
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

    IVoiceChannel getLikelyUserVoiceChannel(IUser user) {
        LongMap<IVoiceState> vs = user.getVoiceStates();
        IVoiceChannel voice = null;
        for (IVoiceState v : vs.values()) {
            IVoiceChannel ch = v.getChannel();
            if (ch != null && user.getClient().getGuilds().contains(ch.getGuild())) {
                //this is the channel we should connect to
                voice = ch;
                break;
            }
        }

        return voice;
    }

    class GuildMusicManager extends AudioEventAdapter {

        private IVoiceChannel currentVoiceChannel;
        public final AudioPlayer player;
        private final BlockingQueue<AudioTrack> queue;

        public GuildMusicManager(AudioPlayerManager manager) {
            player = manager.createPlayer();
            queue = new LinkedBlockingQueue<>();
            player.addListener(this);
        }

        public synchronized void setVolume(int level) {
            player.setVolume(level);
        }

        public synchronized AudioProvider getAudioProvider() {
            return new AudioProvider(player);
        }

        public synchronized void queue(AudioTrack track) {
            if (!player.startTrack(track, true)) {
                queue.offer(track);
            }
        }

        public synchronized boolean hasNextTrack() {
            return queue.peek() != null;
        }

        public synchronized void nextTrack() {
            player.startTrack(queue.poll(), false);
        }

        public synchronized boolean isPlaying() {
            return player.getPlayingTrack() != null;
        }

        @Override
        public synchronized void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
            LogUtil.logInfo("on track end");
//        player.stopTrack();
//        this.nextTrack(); this almost fixed it, just had a huge delay
            switch (endReason) {
                case FINISHED:
                    LogUtil.logInfo("finished");
                    LogUtil.logInfo("still have " + queue.size() + " tracks to play");
                case CLEANUP:
                    LogUtil.logInfo("cleanup");
                case STOPPED:
                    LogUtil.logInfo("stopped");
                    if (endReason.mayStartNext && hasNextTrack()) {
                        LogUtil.logInfo("has next track");
                        nextTrack();
                    } else {
                        LogUtil.logInfo("cant start next or has no next");
                        disconnect();
                    }
                    break;
                case LOAD_FAILED:
                    LogUtil.logInfo("failed");
                    break;
                case REPLACED:
                    LogUtil.logInfo("replaced");
            }
        }

        private void disconnect() {
            if (currentVoiceChannel != null) {
                LogUtil.logInfo("current voice channel is not null");
                LogUtil.logInfo("Try to leave voice channel.");
                player.stopTrack();
                currentVoiceChannel.leave();
                currentVoiceChannel = null;
            }
        }

        public synchronized void setCurrentVoiceChannel(IVoiceChannel channel) {
            LogUtil.logInfo("set current voice channel");
//        currentVoiceChannel = channel;
        }

        public synchronized void userQueue(IChannel channel, IUser user, AudioTrack track) {
            LogUtil.logInfo("user queue");

            IVoiceChannel voice = getLikelyUserVoiceChannel(user);

            if (voice == null) {
                FireAndForget.sendMessage(channel, "You aren't  in any voice channel I can get to!");
            } else {
                if (user.isBot()) {
                    return;
                }

                if (isPlaying() && voice != currentVoiceChannel) {
                    FireAndForget.sendMessage(channel, "You must stop playing to play in a new voice channel.");
                } else {
                    currentVoiceChannel = voice;
                    queue(track);
                }
            }
        }
    }

    private class AudioProvider implements IAudioProvider {
        private final AudioPlayer audioPlayer;
        private AudioFrame lastFrame;

        public AudioProvider(AudioPlayer audioPlayer) {
            this.audioPlayer = audioPlayer;
        }

        @Override
        public boolean isReady() {
//        LogUtil.logInfo("isReady?");
            if (lastFrame == null) {
                lastFrame = audioPlayer.provide();
            }

//        LogUtil.logInfo("" + (lastFrame != null));
            return lastFrame != null;
        }

        @Override
        public byte[] provide() {
//        LogUtil.logInfo("provide");
            if (lastFrame == null) {
                lastFrame = audioPlayer.provide();
            }

            byte[] data = lastFrame != null ? lastFrame.data : null;
            lastFrame = null;

            return data;
        }

        @Override
        public int getChannels() {
            return 2;
        }

        @Override
        public AudioEncodingType getAudioEncodingType() {
            return AudioEncodingType.OPUS;
        }
    }

}
