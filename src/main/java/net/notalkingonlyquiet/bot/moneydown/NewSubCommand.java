package net.notalkingonlyquiet.bot.moneydown;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

@Component
public class NewSubCommand implements MoneyDownSubCommand {
    private static final String HELP = "";

    @Autowired
    private MoneyDownRepository mdRepo;

    @Override
    public String getName() {
        return "new";
    }

    @Override
    public String getDescription() {
        return "Create a new bet. Use \"moneydown new\" for more info.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        //moneydown new ffa for 30
    }
}
