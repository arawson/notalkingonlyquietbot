
package net.notalkingonlyquiet.bot;

import com.sedmelluq.discord.lavaplayer.player.AudioPlayer;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;

/**
 *
 * @author arawson
 */
public class GuildMusicManager {
    public final AudioPlayer player;
    
    public final TrackScheduler scheduler;
    
    public GuildMusicManager(AudioPlayerManager manager) {
        player = manager.createPlayer();
        scheduler = new TrackScheduler(player);
        player.addListener(scheduler);
    }
    
    public AudioProvider getAudioProvider() {
        return new AudioProvider(player);
    }
}
