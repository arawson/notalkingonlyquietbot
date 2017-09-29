
package net.notalkingonlyquiet.bot.core;

import java.util.List;
import net.notalkingonlyquiet.bot.commands.Command;

/**
 *
 * @author arawson
 */
public interface CommandProvider {
	public void initCommands();
	
	public List<Command> getCommands();
}
