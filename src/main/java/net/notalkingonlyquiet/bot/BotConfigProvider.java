package net.notalkingonlyquiet.bot;

import com.google.inject.Singleton;
import com.moandjiezana.toml.Toml;
import java.io.File;
import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.core.ConfigProvider;

/**
 *
 * @author arawson
 */
@Singleton
public class BotConfigProvider implements ConfigProvider {

	private final Config config;

	public BotConfigProvider() {
		Toml toml = new Toml().read(new File("./config.toml"));
		config = toml.to(Config.class);
	}

	@Override
	public Config getConfig() {
		return config;
	}
}
