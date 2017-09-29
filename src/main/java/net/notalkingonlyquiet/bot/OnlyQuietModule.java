
package net.notalkingonlyquiet.bot;

import com.google.inject.AbstractModule;
import net.notalkingonlyquiet.bot.core.BotService;
import net.notalkingonlyquiet.bot.core.ConfigProvider;
import net.notalkingonlyquiet.bot.core.BusProvider;
import net.notalkingonlyquiet.bot.core.ClientProvider;

/**
 *
 * @author arawson
 */
public class OnlyQuietModule extends AbstractModule {

	public OnlyQuietModule() {
	}

	@Override
	protected void configure() {
		
		bind(ClientProvider.class).to(BotClientProvider.class);
		
		bind(ConfigProvider.class).to(BotConfigProvider.class);
		
		bind(BusProvider.class).to(BotBusProvider.class);
		
		bind(BotService.class).to(Bot.class);
	}
	
}
