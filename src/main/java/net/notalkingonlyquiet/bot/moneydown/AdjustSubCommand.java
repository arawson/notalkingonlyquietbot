package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.ArgValidator;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IRole;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;
import java.util.stream.Collectors;

@Component
public class AdjustSubCommand implements MoneyDownSubCommand {
    private static final String BAD_AMOUNT_ERR = "BAD AMOUNT";
    private static final String BAD_USER_ID_ERR = "BAD USER ID ERR";
    private static final ArgValidator VALIDATOR = new ArgValidator()
            .toUpperCase()
            .expectRegexCapture(ArgValidator.USER_ID_PATTERN, BAD_USER_ID_ERR)
            .expectInt(BAD_AMOUNT_ERR);
    private static final String HELP = "moneydown adjust <@Username> <NEW AMOUNT>";

    @Autowired
    private TransactionRepository tRepo;

    @Override
    public String getName() {
        return "ADJUST";
    }

    @Override
    public String getDescription() {
        return "Admin tool to set a user's balance.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IDiscordClient client = event.getClient();
        final IUser admin = event.getAuthor();
        final IGuild guild = event.getGuild();
        final IChannel channel = event.getChannel();

        ArgValidator.Result parse = VALIDATOR.parse(args);

        if (!parse.ok) {
            StringBuilder s = new StringBuilder();
            for (String e: parse.errors) {
                switch (e) {
                    case ArgValidator.EXPECTED_MORE_ERROR:
                        s.append("\nNeed more arguments!");
                        break;
                    case BAD_AMOUNT_ERR:
                        s.append("\nThe amount to adjust must be a positive whole number.");
                        break;
                    case BAD_USER_ID_ERR:
                        s.append("\nYou must @ mention which user's balance you are adjusting.");
                        break;
                    case ArgValidator.TOO_MANY_ERROR:
                        s.append("\nToo many arguments.");
                        break;
                }
            }
            s.append("\n");
            s.append(HELP);
            FireAndForget.sendMessage(channel, s.toString());
        } else {
            final long amount = (Integer)parse.results.get(1);
            final IUser user = client.getUserByID(Long.parseLong((String)parse.results.get(0)));

            //TODO: allow customizable banker role
            List<Long> adminRoles =
                    guild.getRoles().stream()
                            .filter(r -> r.getName().toUpperCase().contains("ADMIN"))
                            .map(r -> r.getLongID())
                            .collect(Collectors.toList());

            if (admin.isBot() ||
                    admin.getRolesForGuild(guild).stream().noneMatch(r -> adminRoles.contains(r.getLongID()))) {
                FireAndForget.sendMessage(channel, "You must be an admin to adjust balances.");
            } else if (amount < 0) {
                FireAndForget.sendMessage(channel, "You can't set a user's balance below 0.");
            } else if (user.isBot()) {
                FireAndForget.sendMessage(channel, "Bots don't have balances to set.");
            } else {
                //guard against no-balance
                Util.initSingleUserOnGuild(user, guild, tRepo);

                //TODO: lock this somehow during adjustment
                try {
                    long currentAmount = tRepo.sumAllTransactionsForUserInGuild(user.getLongID(), guild.getLongID());

                    long delta = amount - currentAmount;

                    Transaction t = new Transaction(user, guild, delta, TransactionType.ADJUSTMENT);

                    tRepo.save(t);

                    FireAndForget.sendMessage(channel, admin.getName() + " has set " +
                        user.getName() + "'s balance to " + amount);
                } catch (DataAccessException ex) {
                    FireAndForget.sendMessage(channel, "Unable to adjust user balance. Tell the bot" +
                            " maintainer about this.");
                }
            }
        }
    }
}
