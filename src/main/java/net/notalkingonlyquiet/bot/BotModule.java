
package net.notalkingonlyquiet.bot;

import com.google.inject.AbstractModule;
import net.notalkingonlyquiet.bot.audio.AudioService;
import net.notalkingonlyquiet.bot.audio.LavaPlayerAudioService;
import net.notalkingonlyquiet.bot.core.BotService;
import net.notalkingonlyquiet.bot.core.ConfigProvider;
import net.notalkingonlyquiet.bot.core.BusProvider;
import net.notalkingonlyquiet.bot.core.ClientProvider;
import net.notalkingonlyquiet.bot.core.CommandProvider;

/**
 *
 * @author arawson
 */
public class BotModule extends AbstractModule {

	public BotModule() {
	}

	@Override
	protected void configure() {
		bind(AudioService.class).to(LavaPlayerAudioService.class);
		
		bind(CommandProvider.class).to(ScanningBotCommandProvider.class);
		
		bind(ClientProvider.class).to(BotClientProvider.class);
		
		bind(ConfigProvider.class).to(BotConfigProvider.class);
		
		bind(BusProvider.class).to(BotBusProvider.class);
		
		bind(BotService.class).to(Bot.class);
	}
	
}
