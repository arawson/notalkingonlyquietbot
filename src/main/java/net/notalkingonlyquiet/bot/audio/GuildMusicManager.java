package net.notalkingonlyquiet.bot.audio;

import com.google.common.eventbus.EventBus;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.event.AudioEventAdapter;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.util.concurrent.BlockingQueue;
import java.util.concurrent.LinkedBlockingQueue;
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
    private final EventBus hostBus;

    public GuildMusicManager(AudioPlayerManager manager, EventBus hostBus) {
        player = manager.createPlayer();
        queue = new LinkedBlockingQueue<>();
        player.addListener(this);
        this.hostBus = hostBus;
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
        return queue.poll() != null;
    }

    public synchronized void nextTrack() {
        player.startTrack(queue.poll(), false);
    }
    
    public synchronized boolean isPlaying() {
        return player.getPlayingTrack() != null;
    }

    @Override
    public synchronized void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason) {
        if (endReason.mayStartNext) {
            if (hasNextTrack()) {
                nextTrack();
            } else {
                //send signal to exit audio channel
            }
        }
    }

    public synchronized void userQueue(IChannel channel, IUser user) throws
            MissingPermissionsException, RateLimitException, DiscordException {
        if (user.getConnectedVoiceChannels().size() < 1) {
            channel.sendMessage("You aren't in a voice channel!");
        } else {
            //TODO: filter out other bots
            //TODO: is it possible for normal users to connect to multiple voice channels at once?
            IVoiceChannel newVoice = user.getConnectedVoiceChannels().get(0);
            if (isPlaying() && newVoice != currentVoiceChannel) {
                channel.sendMessage("You must stop playing to play in a new voice channel.");
            } else {
                
            }
        }
    }
}
