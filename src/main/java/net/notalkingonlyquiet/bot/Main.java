package net.notalkingonlyquiet.bot;

import com.google.inject.Guice;
import com.google.inject.Inject;
import com.google.inject.Injector;
import java.io.IOException;
import net.notalkingonlyquiet.bot.core.BusProvider;
import net.notalkingonlyquiet.bot.core.events.StartEvent;
import net.notalkingonlyquiet.bot.core.events.StopEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public final class Main {
	
	private final BusProvider busProvider;
	private final Bot bot;

	@Inject
	public Main(BusProvider busProvider, Bot bot) {
		this.busProvider = busProvider;
		this.bot = bot;
	}
	
	private synchronized void start() {
		LogUtil.logInfo("Starting the bot...");
		LogUtil.logInfo("busProvider: " + busProvider.toString());
		busProvider.getBus().post(new StartEvent());
	}
	
	private synchronized void stop() {
		busProvider.getBus().post(new StopEvent());
	}

    /**
     * @param args the command line arguments
     * @throws sx.blah.discord.util.DiscordException
     * @throws sx.blah.discord.util.RateLimitException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws DiscordException, RateLimitException, IOException {
		final Injector injector = Guice.createInjector(new OnlyQuietModule());
		final Main m = injector.getInstance(Main.class);
		
		Runtime.getRuntime().addShutdownHook(new Thread() {
			@Override
			public void run() {
				LogUtil.logInfo("Got external shutdown signal...");
				m.stop();
			}
		});
		
		m.start();
    }
}
