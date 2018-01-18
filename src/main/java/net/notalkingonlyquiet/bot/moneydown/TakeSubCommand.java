package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.ArgValidator;
import net.notalkingonlyquiet.bot.util.CommandUtil;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import net.notalkingonlyquiet.bot.util.LogUtil;
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

import javax.persistence.OptimisticLockException;
import java.sql.Timestamp;
import java.util.List;

@Component
public class TakeSubCommand implements MoneyDownSubCommand {
    private final static String NO_ID_ERR = "NO ID";
    private final static String BAD_ACTION_ERR = "BAD ACTION";
    private static final ArgValidator VALIDATOR = new ArgValidator()
            .toUpperCase()
            .expectInt(NO_ID_ERR)
            .expectChoice(BAD_ACTION_ERR, "FOR", "AGAINST");
    private static final String HELP = "moneydown take <ID> <FOR or AGAINST>";

    @Autowired
    private MoneyDownRepository mdRepo;

    @Autowired
    private TransactionRepository tRepo;

    @Override
    public String getName() {
        return "TAKE";
    }

    @Override
    public String getDescription() {
        return "Take the bet! moneydown take <ID> <FOR or AGAINST>";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IUser user = event.getAuthor();
        final IGuild guild = event.getGuild();
        final IChannel channel = event.getChannel();

        ArgValidator.Result parse = VALIDATOR.parse(args);

        if (!parse.ok) {
            StringBuilder e = new StringBuilder();
            for (String s: parse.errors) {
                switch (s) {
                    case NO_ID_ERR:
                        e.append("\nYou must enter an ID that is a positive whole number.");
                        break;
                    case BAD_ACTION_ERR:
                        e.append("\nYou must enter FOR or AGAINST when taking a bet.");
                        break;
                    case ArgValidator.EXPECTED_MORE_ERROR:
                        e.append(HELP);
                        break;
                    case ArgValidator.TOO_MANY_ERROR:
                        e.append("\nToo many arguments.");
                        break;
                }
            }

            FireAndForget.sendMessage(channel, e.toString());
        } else {
            final long id = (Integer)parse.results.get(0);
            final boolean isFor = ((String)parse.results.get(1)).equals("FOR");
            final Timestamp now = Util.getNow();

            MoneyDown m = null;
            try {
                m = mdRepo.findOne(id);
            } catch (DataAccessException ex) {
                FireAndForget.sendMessage(channel, "Could not access database. Contact the bot maintainer.");
                throw ex;
            }

            if (m == null || m.getGuildID() != guild.getLongID()) {
                FireAndForget.sendMessage(channel, "Could not find moneydown #" + id);
            } else if (m.getParticipants().stream().anyMatch(p -> p.getUserID() == user.getLongID())) {
                FireAndForget.sendMessage(channel, "You have already taken that bet.");
            } else if (m.getLastTakeableTime().before(now)) {
                FireAndForget.sendMessage(channel, "It's too late to join this bet!");
            } else if (m.isCancelled()) {
                FireAndForget.sendMessage(channel, "That bet has been cancelled.");
            } else if (m.isResolved()) {
                FireAndForget.sendMessage(channel, "That bet has already concluded.");
            } else {
                Participant p = new Participant(user, isFor);

                for (int i = 0; i < 3; i++ ) {
                    m = mdRepo.findOne(id);
                    m.getParticipants().add(p);

                    try {
                        mdRepo.save(m);
                        break;
                    } catch (OptimisticLockException ex) {
                        //TODO: log poperly
                        LogUtil.logWarning("Exception when writing moneydown, " + ex.getLocalizedMessage());
                        LogUtil.logWarning("At try #" + i);
                        if (i == 2) {
                            FireAndForget.sendMessage(channel, "Unable to update the bet. Contact the bot maintainer.");
                            throw ex;
                        }
                    }
                }

                StringBuilder msg = new StringBuilder();
                msg.append("\n")
                        .append(user.getName())
                        .append(" has taken bet #")
                        .append(id)
                        .append(" for ")
                        .append(m.getEntryCost())
                        .append(" money!");

                long balance = Util.getUserBalanceInGuild(user, guild, tRepo);

                if (balance - m.getEntryCost() < 0) {
                    msg.append("\nIf they lose they will be in the trash!");
                }

                if (CommandUtil.chance(0.05)) {
                    msg.append("\nPay the front line, take the don'ts.");
                }

                FireAndForget.sendMessage(channel, msg.toString());
            }
        }
    }
}
