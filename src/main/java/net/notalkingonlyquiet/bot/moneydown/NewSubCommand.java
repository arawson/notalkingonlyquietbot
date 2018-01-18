package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.ArgValidator;
import net.notalkingonlyquiet.bot.util.CommandUtil;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

@Component
public class NewSubCommand implements MoneyDownSubCommand {
    private static final String BAD_OPTION_ERR = "BAD OPTION";
    private static final String BAD_FORM_ERR = "BAD FORM";
    private static final String MISSING_AMOUNT_ERR = "MISSING AMOUNT";
    private static final ArgValidator VALIDATOR = new ArgValidator()
            .toUpperCase()
            .expectChoice(BAD_OPTION_ERR, "FFA", "BET")
            .expectLiteral("FOR", BAD_FORM_ERR)
            .expectInt(MISSING_AMOUNT_ERR)
            .expectRemainder();
    private static final String HELP =
        "moneydown new <FFA or BET> for <AMOUNT> <DESCRIPTION>\n" +
        "   <AMOUNT> must be some positive whole number\n" +
        "   <DESCRIPTION> should be a good description of the bet\n" +
        "   You will get the moneydown ID once it is saved to the record.";

    @Autowired
    private MoneyDownRepository mdRepo;

    @Autowired
    private TransactionRepository tRepo;

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
        final IDiscordClient client = event.getClient();
        final IGuild guild = event.getGuild();
        final IUser user = event.getAuthor();
        final IChannel channel = event.getChannel();

        ArgValidator.Result parse = VALIDATOR.parse(args);
        
        StringBuilder errorBuilder = new StringBuilder();
        if (!parse.ok) {
            if (args.size() == 0) {
                errorBuilder.append(HELP);
            } else {
                for (String err : parse.errors) {
                    switch (err) {
                        case BAD_OPTION_ERR:
                            errorBuilder.append("\nThe type must be FFA or BET");
                            break;
                        case BAD_FORM_ERR:
                            errorBuilder.append("\nThe word FOR must come after the type.");
                            break;
                        case MISSING_AMOUNT_ERR:
                            errorBuilder.append("\nThe amount must be a positive whole number.");
                            break;
                        default:
                            break;
                    }
                }
            }

            FireAndForget.sendMessage(channel, errorBuilder.toString());
        } else {
            BetType type = BetType.valueOf((String) parse.results.get(0));
            int entryCost = (Integer) parse.results.get(2);
            String description = (String) parse.results.get(3);

            if (entryCost <= 0) {
                errorBuilder.append("\nYou must be more than 0 money.");
            }

            if (description == null || description.trim().equals("")) {
                errorBuilder.append("\nYou *must* enter a description.");
            }

            if (errorBuilder.length() > 0) {
                //print out errors and stop
                FireAndForget.sendMessage(channel, errorBuilder.toString());
            } else {
                try {
                    //load it in! (and send the ID and time limits of the new bet back)
                    MoneyDown moneyDown = new MoneyDown(guild, user, type, description, (long) entryCost);
                    moneyDown.getParticipants().add(
                            new Participant(user, type == BetType.BET ? true : null));

                    //this should give us back the entity, now with ID!
                    moneyDown = mdRepo.save(moneyDown);

                    StringBuilder msg = new StringBuilder();

                    if (CommandUtil.chance(0.05)) {
                        msg.append("\nPlace your bets now ladies and gentlemen!");
                    }

                    msg.append("\nMoney down #")
                            .append(moneyDown.getId())
                            .append(":");

                    msg.append("\n")
                            .append(user.getName()).append(" has thrown down ")
                            .append(moneyDown.getEntryCost())
                            .append(" money on \"")
                            .append(moneyDown.getDescription())
                            .append("\".");


                    long balance = Util.getUserBalanceInGuild(user, guild, tRepo);

                    if (balance - moneyDown.getEntryCost() < 0) {
                        msg.append("\nIf they lose they will be in the trash!");
                    }

                    msg.append("\nYou have until ")
                            .append(moneyDown.getLastTakeableTime())
                            //TODO: handle time zones
                            //TODO: better time and date formatting
                            .append(" to take the bet!");

                    msg.append("\nAny takers?");

                    FireAndForget.sendMessage(channel, msg.toString());
                } catch (DataAccessException ex) {
                    FireAndForget.sendMessage(channel, "Unable to save moneydown. Contact the bot maintainer!");
                    throw ex;
                }
            }
        }
    }
}
