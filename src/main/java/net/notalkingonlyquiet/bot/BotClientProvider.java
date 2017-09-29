package net.notalkingonlyquiet.bot;

import com.google.common.eventbus.Subscribe;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.notalkingonlyquiet.bot.core.BusProvider;
import net.notalkingonlyquiet.bot.core.ClientProvider;
import net.notalkingonlyquiet.bot.core.ConfigProvider;
import net.notalkingonlyquiet.bot.core.events.ClientAbortEvent;
import net.notalkingonlyquiet.bot.core.events.StartEvent;
import net.notalkingonlyquiet.bot.core.events.StopEvent;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
@Singleton
public class BotClientProvider implements ClientProvider {

	private final IDiscordClient client;

	@Inject
	public BotClientProvider(BusProvider bus, ConfigProvider configProvider) throws DiscordException {
		LogUtil.logInfo("Attempting to connect to Discord...");

		client = new ClientBuilder().withToken(configProvider.getConfig().login.token).build();

		bus.getBus().register(this);
	}

	@Override
	public IDiscordClient getClient() {
		return client;
	}

	@Override
	public void start() throws DiscordException, RateLimitException {
		client.login();
	}
	
	@Subscribe
	public void handleStop(StopEvent e) {
		try {
			client.logout();
		} catch (DiscordException ex) {
			LogUtil.logError(ex.getErrorMessage());
		}
	}

	@Subscribe
	public void handleClientAbort(ClientAbortEvent e) {
		try {
			client.logout();
		} catch (DiscordException ex) {
			LogUtil.logError(ex.getErrorMessage());
		}
	}

	@Subscribe
	public void handleStartEvent(StartEvent e) {
		LogUtil.logInfo("Starting discord client...");
		try {
			client.login();
		} catch (DiscordException | RateLimitException ex) {
			Logger.getLogger(BotClientProvider.class.getName()).log(Level.SEVERE, null, ex);
			throw new RuntimeException(ex);
		}
		LogUtil.logInfo("Login Successful...");
	}
}
