package net.notalkingonlyquiet.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.net.URL;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import net.notalkingonlyquiet.bot.LogUtil;
import net.notalkingonlyquiet.bot.core.BotService;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 *
 * @author arawson
 */
public class DefaultGuildMusicManager extends AudioEventAdapter
        implements GuildMusicManager {

    private IVoiceChannel currentVoiceChannel;
    private final AudioPlayer player;
    private final AudioPlayerManager playerManager;
    private final BlockingQueue<AudioTrack> queue;
    private final BotService botService;

    DefaultGuildMusicManager(AudioPlayerManager manager, BotService botService) {
        this.playerManager = manager;
        this.botService = botService;
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

    private synchronized String userQueue(IUser user, AudioTrack track) {
        String returnMessage = null;
        LogUtil.logInfo("user queue");
        if (user.getConnectedVoiceChannels().size() < 1) {
            returnMessage = "You aren't  in a voice channel!";
        } else {
            if (user.isBot()) {
                returnMessage = "User is bot.";
            } else {

                IVoiceChannel newVoice = user.getConnectedVoiceChannels().get(0);
                if (isPlaying() && newVoice != currentVoiceChannel) {
                    returnMessage = "You must stop playing to play in a new voice channel.";
                } else {
                    currentVoiceChannel = newVoice;
                    queue(track);
                }
            }
        }
        
        return returnMessage;
    }

    @Override
    public void playURL(IUser requester, URL url, PlayFeedbackListener listener) {
        playerManager.loadItemOrdered(this, url.toString(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                String r = userQueue(requester, track);
                //TODO: feedback on error
                listener.trackLoaded(r);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }

                String r = userQueue(requester, firstTrack);
                
                listener.playlistLoaded(r);
            }

            @Override
            public void noMatches() {
                listener.noMatches();
            }

            @Override
            public void loadFailed(FriendlyException fe) {
                listener.LoadFailed(fe);
            }
        });
    }
}
