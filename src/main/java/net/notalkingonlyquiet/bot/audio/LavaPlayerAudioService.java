package net.notalkingonlyquiet.bot.audio;

import com.google.inject.Inject;
import com.sedmelluq.discord.lavaplayer.player.AudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.player.DefaultAudioPlayerManager;
import com.sedmelluq.discord.lavaplayer.source.AudioSourceManagers;
import java.util.HashMap;
import java.util.Map;
import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.core.BotService;
import org.apache.http.client.config.RequestConfig;
import sx.blah.discord.handle.obj.IGuild;

/**
 *
 * @author arawson
 */
public class LavaPlayerAudioService implements AudioService {

    private final BotService botService;
    private final Map<IGuild, DefaultGuildMusicManager> musicManagers = new HashMap<>();
    private final AudioPlayerManager playerManager = new DefaultAudioPlayerManager();

    @Inject
    public LavaPlayerAudioService(Config config, BotService botService) {
        this.botService = botService;
        playerManager.setHttpRequestConfigurator(
                (cfg) -> RequestConfig.copy(cfg)
                        .setConnectTimeout(config.performance.timeout)
                        .build());

        AudioSourceManagers.registerRemoteSources(playerManager);
        AudioSourceManagers.registerLocalSource(playerManager);
    }

    @Override
    public DefaultGuildMusicManager getGuildMusicManager(IGuild guild) {
        DefaultGuildMusicManager mm = musicManagers.get(guild);
        if (mm == null) {
            mm = new DefaultGuildMusicManager(playerManager, botService);
            musicManagers.put(guild, mm);
        }

        guild.getAudioManager().setAudioProvider(mm.getAudioProvider());

        return mm;
    }
}
