
package net.notalkingonlyquiet.bot;

import com.google.inject.Inject;
import com.google.inject.Injector;
import java.util.ArrayList;
import java.util.Collections;
import java.util.List;
import java.util.Set;
import net.notalkingonlyquiet.bot.commands.Command;
import net.notalkingonlyquiet.bot.core.CommandProvider;
import org.reflections.Reflections;

/**
 *
 * @author arawson
 */
public class ScanningBotCommandProvider implements CommandProvider {
	
	private final List<Command> commands = new ArrayList<>();
	
	private final Injector injector;

	@Inject
	public ScanningBotCommandProvider(Injector injector) {
		this.injector = injector;
		LogUtil.logInfo("Injector is " + injector.toString());
	}

	@Override
	public void initCommands() {
		Reflections reflections = new Reflections("net.notalkingonlyquiet");
		
		Set<Class<? extends Command>> cset = reflections.getSubTypesOf(Command.class);
		
		for (Class<? extends Command> class1 : cset) {
			Command c = injector.getInstance(class1);
			commands.add(c);
		}
	}

	@Override
	public List<Command> getCommands() {
		return Collections.unmodifiableList(commands);
	}
	
}
