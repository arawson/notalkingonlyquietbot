
package net.notalkingonlyquiet.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import com.sedmelluq.discord.lavaplayer.track.AudioTrackEndReason;
import java.net.URL;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 *
 * @author arawson
 */
public interface GuildMusicManager {

    boolean hasNextTrack();

    boolean isPlaying();

    void nextTrack();

    //TODO: don't expose lavaplayer specifics
    void onTrackEnd(AudioPlayer player, AudioTrack track, AudioTrackEndReason endReason);

    void setCurrentVoiceChannel(IVoiceChannel channel);

    void setVolume(int level);

    public void playURL(IUser requester, URL url, PlayFeedbackListener listener);

    public interface PlayFeedbackListener {

        public void trackLoaded(String identifier);
        
        public void playlistLoaded(String name);
        
        public void noMatches();
        
        public void LoadFailed(Exception e);
    }
}
