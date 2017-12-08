package net.notalkingonlyquiet.bot.audio;

import com.google.common.eventbus.EventBus;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
import net.notalkingonlyquiet.bot.Bot;
import net.notalkingonlyquiet.bot.FireAndForget;
import net.notalkingonlyquiet.bot.LogUtil;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
public class GuildMusicManager extends AudioEventAdapter {

    private IVoiceChannel currentVoiceChannel;
    public final AudioPlayer player;
    private final BlockingQueue<AudioTrack> queue;
	private final Bot bot;
    private final EventBus hostBus;

    public GuildMusicManager(AudioPlayerManager manager, EventBus hostBus, Bot bot) {
        player = manager.createPlayer();
        queue = new LinkedBlockingQueue<>();
        this.hostBus = hostBus;
		this.bot = bot;
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
		
		IVoiceChannel voice = bot.getLikelyUserVoiceChannel(user);
		
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
