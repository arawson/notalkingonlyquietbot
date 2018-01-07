package net.notalkingonlyquiet.bot.util;

import net.notalkingonlyquiet.bot.application.Command;
import net.notalkingonlyquiet.bot.application.RootCommand;

import java.util.ArrayList;
import java.util.List;

public class CommandUtil {
    public final static String NEW_HELP = "OwO what's this? Help hasn't generated. You should probably contact the guy running the bot. Probably...";

    public static String generateHelp(List<? extends Command> commands) {

        final List<? extends Command> sortedCommands = new ArrayList<>(commands);
        sortedCommands.sort((Command r1, Command r2)->r1.getName().compareTo(r1.getName()));

        final StringBuilder helpBuilder = new StringBuilder();
        helpBuilder.append("Use help or a blank command to get this help.\n");
        helpBuilder.append("These are the commands currently registered:\n");
        for (Command c: sortedCommands) {
            helpBuilder.append(c.getName().toUpperCase())
                    .append(" -> ")
                    .append(c.getDescription())
                    .append("\n");
        }

        return helpBuilder.toString();
    }
}
