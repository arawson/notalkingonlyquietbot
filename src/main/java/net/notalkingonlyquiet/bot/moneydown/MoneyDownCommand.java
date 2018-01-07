package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.application.Command;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

public class MoneyDownCommand implements Command {

    @Override
    public String getName() {
        return "moneydown";
    }

    @Override
    public String getDescription() {
        return "";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {

    }
}
