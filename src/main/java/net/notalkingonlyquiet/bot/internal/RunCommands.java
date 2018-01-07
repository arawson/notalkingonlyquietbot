package net.notalkingonlyquiet.bot.internal;

import com.google.common.base.MoreObjects;
import com.google.common.collect.Lists;
import net.notalkingonlyquiet.bot.application.Command;
import net.notalkingonlyquiet.bot.application.RootCommand;
import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.util.CommandUtil;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import net.notalkingonlyquiet.bot.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;

import java.util.*;
import java.util.concurrent.ConcurrentHashMap;

@Component
@Scope(value = "singleton")
public class RunCommands implements BotInternalProc {
    private final String prefix;
    private final Map<String, Command> commands = new ConcurrentHashMap<>();
    private String help = CommandUtil.NEW_HELP;

    @Autowired
    public RunCommands(IDiscordClient client, ComponentWaiter cw, Config config) {
        cw.notReady(getClass());
        prefix = config.bot.prefix;
        client.getDispatcher().registerListener(this);
        cw.ready(getClass());
    }

    @Autowired
    public void setCommands(List<RootCommand> injectedCommands) {
        commands.clear();

        for (Command c: injectedCommands) {
            commands.put(c.getName().toUpperCase(), c);
        }

        synchronized (this) {
            help = CommandUtil.generateHelp(injectedCommands);
        }
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("commands", commands)
                .toString();
    }

    @EventSubscriber
    public void onMessage(MessageReceivedEvent event) {
        System.out.println(commands);
        final IMessage message = event.getMessage();
        final IChannel channel = message.getChannel();
        final IUser user = message.getAuthor();

        //we don't talk to bots
        if (user.isBot()) {
            return;
        }

        //LogUtil.logInfo(message.getGuild().getName() + ":" + channel.getName() + ":" + user.getName() + ": " + message.getContent());

        final List<String> line = Lists.newArrayList(message.getContent().split(" "));

        if (line.size() >= 1 && line.get(0).startsWith(prefix)) {
            final String cmd = line.get(0).replaceFirst(prefix, "").toUpperCase();
            final List<String> args = line.subList(1, line.size());

            Command command = commands.get(cmd);
            if (cmd.equals("HELP") || cmd.equals("")) {
                printHelp(channel);
            }
            else if (command == null) {
                FireAndForget.sendMessage(channel, "I don't recognize that command. " + cmd);
            }
            else {
                command.execute(args, event);
            }

        }
    }

    private void printHelp(IChannel channel) {
        synchronized (this) {
            FireAndForget.sendMessage(channel, help);
        }
    }
}
