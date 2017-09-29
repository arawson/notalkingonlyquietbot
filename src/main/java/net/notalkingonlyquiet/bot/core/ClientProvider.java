
package net.notalkingonlyquiet.bot.core;

import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
public interface ClientProvider {
	public IDiscordClient getClient();
	
	public void start() throws DiscordException, RateLimitException;
}
