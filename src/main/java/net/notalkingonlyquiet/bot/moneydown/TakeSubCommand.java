package net.notalkingonlyquiet.bot.moneydown;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

@Component
public class TakeSubCommand implements MoneyDownSubCommand {
    @Autowired
    private MoneyDownRepository mdRepo;

    @Override
    public String getName() {
        return "take";
    }

    @Override
    public String getDescription() {
        return "Take the bet! moneydown take <ID>";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {

    }
}
