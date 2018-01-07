package net.notalkingonlyquiet.bot.memes;

import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import net.notalkingonlyquiet.bot.application.Command;
import net.notalkingonlyquiet.bot.application.RootCommand;
import net.notalkingonlyquiet.bot.util.CommandUtil;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

@Component
public class MemeCommand implements RootCommand {
    @Autowired
    private AutoMemeCommand auto;

    private final Map<String, MemeSubCommand> subCommands = new HashMap<>();
    private String help = CommandUtil.NEW_HELP;

    @Autowired
    public void setCommands(List<MemeSubCommand> injectedCommands) {
        subCommands.clear();

        for (MemeSubCommand c : injectedCommands) {
            subCommands.put(c.getName().toUpperCase(), c);
        }

        synchronized (this) {
            help = CommandUtil.generateHelp(injectedCommands);
        }
    }

    @Override
    public String getName() {
        return "meme";
    }

    @Override
    public String getDescription() {
        return "the main meme command, for all your memeing needs";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        if (args.size() == 0) {
            auto.execute(args, event);
        } else {
            String subC = args.get(0).toUpperCase();
            List<String> subArgs = args.subList(1, args.size());

            MemeSubCommand msc = subCommands.get(subC);
            if (subC.equals("HELP")) {
                printHelp(event.getChannel());
            } else if (msc != null) {
                msc.execute(subArgs, event);
            } else {
                FireAndForget.sendMessage(event.getChannel(), "I don't recognize this meme command: \"" + subC + "\"");
            }
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("commands", subCommands)
                .toString();
    }

    private void printHelp(IChannel channel) {
        synchronized (this) {
            FireAndForget.sendMessage(channel, help);
        }
    }
}
