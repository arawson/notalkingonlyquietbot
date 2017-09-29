package net.notalkingonlyquiet.bot;

import com.google.common.eventbus.AsyncEventBus;
import com.google.common.eventbus.EventBus;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.util.concurrent.Executor;
import java.util.concurrent.ScheduledThreadPoolExecutor;
import net.notalkingonlyquiet.bot.core.ConfigProvider;
import net.notalkingonlyquiet.bot.core.BusProvider;

/**
 *
 * @author arawson
 */
@Singleton
public class BotBusProvider implements BusProvider {

	private final Executor busExecutor;
	private final EventBus eventBus;

	@Inject
	public BotBusProvider(ConfigProvider cls) {
		busExecutor = new ScheduledThreadPoolExecutor(cls.getConfig().performance.threads);
		eventBus = new AsyncEventBus(busExecutor);
	}

	@Override
	public EventBus getBus() {
		return eventBus;
	}

}
