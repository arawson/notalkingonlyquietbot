package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.application.Command;
import net.notalkingonlyquiet.bot.application.RootCommand;
import net.notalkingonlyquiet.bot.util.CommandUtil;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

public class MoneyDownCommand implements RootCommand {
    private final Map<String, MoneyDownSubCommand> subCommands = new HashMap<>();
    private String help = CommandUtil.NEW_HELP;

    @Autowired
    public void setSubCommands(List<MoneyDownSubCommand> injectedCommands) {
        subCommands.clear();

        for (MoneyDownSubCommand c: injectedCommands) {
            subCommands.put(c.getName(), c);
        }

        synchronized (this) {
            help = CommandUtil.generateHelp(injectedCommands);
        }
    }

    @Override
    public String getName() {
        return "moneydown";
    }

    @Override
    public String getDescription() {
        return "Bet fake money on something. They're just plastic!";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IChannel channel = event.getChannel();

        if (args.size() == 0 || args.get(0).toUpperCase().equals("HELP")) {
            synchronized (this) {
                FireAndForget.sendMessage(channel, help);
            }
        } else {
            String cmdWord = args.get(0);
            List<String> newArgs = args.subList(1, args.size());

            MoneyDownSubCommand cmd = subCommands.get(cmdWord);

            if (cmd == null) {
                FireAndForget.sendMessage(channel, "I don't recognize that command: " + cmdWord);
            } else {
                cmd.execute(newArgs, event);
            }
        }
    }
}
