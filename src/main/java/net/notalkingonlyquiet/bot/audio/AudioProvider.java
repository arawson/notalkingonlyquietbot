
package net.notalkingonlyquiet.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.track.playback.AudioFrame;
import sx.blah.discord.handle.audio.IAudioProvider;

/**
 *
 * @author arawson
 */
public class AudioProvider implements IAudioProvider {
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
