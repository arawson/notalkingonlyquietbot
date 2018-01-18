package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

@Component
public class BankSubCommand implements MoneyDownSubCommand {
    @Autowired
    private TransactionRepository tRepo;

    @Override
    public String getName() {
        return "BANK";
    }

    @Override
    public String getDescription() {
        return "Shows you how much money you have in the bank.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IUser user = event.getAuthor();
        final IChannel channel = event.getChannel();
        final IGuild guild = event.getGuild();

        Util.initSingleUserOnGuild(user, guild, tRepo);

        try {
            final StringBuilder r = new StringBuilder()
                    .append("\n")
                    .append(user.getName())
                    .append(" you have ")
                    .append(tRepo.sumAllTransactionsForUserInGuild(user.getLongID(), guild.getLongID()))
                    .append(" money.");

            FireAndForget.sendMessage(channel, r.toString());
        } catch (DataAccessException ex) {
            FireAndForget.sendMessage(channel, "Could not get your balance. Contact the bot maintainer!");
            throw ex;
        }
    }
}
