package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.ArgValidator;
import net.notalkingonlyquiet.bot.util.CommandUtil;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import net.notalkingonlyquiet.bot.util.LogUtil;
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

import javax.persistence.OptimisticLockException;
import java.util.ArrayList;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class ConcludeSubCommand implements MoneyDownSubCommand {
    private static final String BAD_ID_ERR = "BAD ID";
    private static final String BAD_WIN_LOSE_ERROR = "BAD WIN LOSE";
    private static final String BAD_USER_ID_ERR = "BAD USER ID";
    private static final ArgValidator VALIDATOR1 = new ArgValidator()
            .toUpperCase()
            .expectInt(BAD_ID_ERR)
            .expectChoice(BAD_WIN_LOSE_ERROR, "SUCCESS", "FAILURE");
    private static final ArgValidator VALIDATOR2 = new ArgValidator()
            .toUpperCase()
            .expectInt(BAD_ID_ERR)
            .expectRegexCapture(ArgValidator.USER_ID_PATTERN, BAD_USER_ID_ERR);
    private static final String HELP =
            "\nmoneydown conclude <ID> <SUCCESS or FAILURE>" +
            "\nmoneydown conclude <ID> <@WINNER>";

    @Autowired
    private MoneyDownRepository mdRepo;

    @Autowired
    private TransactionRepository tRepo;

    @Override
    public String getName() {
        return "CONCLUDE";
    }

    @Override
    public String getDescription() {
        return "Mark a bet as completed.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IDiscordClient client = event.getClient();
        final IGuild guild = event.getGuild();
        final IUser user = event.getAuthor();
        final IChannel channel = event.getChannel();

        if (CommandUtil.chance(0.05)) {
            FireAndForget.sendMessage(channel, "I'm concluding!");
        }

        ArgValidator.Result parse1 = VALIDATOR1.parse(args);
        ArgValidator.Result parse2 = VALIDATOR2.parse(args);

        if (!parse1.ok && !parse2.ok) {
            FireAndForget.sendMessage(channel, HELP);
            return;
        }

        final long betId = (Integer) (parse1.ok ? parse1.results.get(0) : parse2.results.get(0));
        final String passFail = (String) (parse1.ok ? parse1.results.get(1) : null);
        final long winnerId = parse2.ok ? Long.parseLong((String)parse2.results.get(1)) : -1;

        try {
            MoneyDown md = mdRepo.findOne(betId);

            if (md == null || md.getGuildID() != guild.getLongID()) {
                FireAndForget.sendMessage(channel, "Could not find that bet.");
                return;
            } else if (md.isResolved()) {
                FireAndForget.sendMessage(channel, "That bet is already resolved.");
                return;
            } else if (md.isCancelled()) {
                FireAndForget.sendMessage(channel, "That bet has been cancelled.");
            }

            final List<Long> winners = new ArrayList<>();
            final List<Long> trashed = new ArrayList<>();
            boolean ffaAllLose = false;

            if (winnerId != -1) {
                winners.add(winnerId);
            }
            if ("SUCCESS".equals(passFail)) {
                if (md.getType() == BetType.FFA) {
                    FireAndForget.sendMessage(channel, "You must @ reference a winner for a free for all bet.");
                    return;
                }
                winners.addAll(
                        md.getParticipants().stream().filter(p -> p.isInAgreement()).map(p -> p.getUserID()).collect(Collectors.toList())
                );
            }
            if ("FAILURE".equals(passFail)) {
                if (md.getType() == BetType.FFA) {
                    //special, everone loses condition
                    ffaAllLose = true;
                } else {
                    winners.addAll(
                            md.getParticipants().stream().filter(p -> !p.isInAgreement()).map(p -> p.getUserID()).collect(Collectors.toList())
                    );
                }
            }

            //there shouldn't be any cases outside of FFA where no one wins
            //for now, we just let money vanish into the ether
            final long pot = md.getPot();
            final long perUserWinning = pot / (winners.size() == 0 ? 1 : winners.size());
            //TODO: decide what to do with remainder
            final long remainderPot = pot - (perUserWinning * winners.size());
            final long cost = md.getEntryCost();

            final List<Transaction> takeTransactions = new ArrayList<>(md.getParticipants().size());
            final List<Transaction> rewardTransactions = new ArrayList<>(winners.size());
            for (Participant p : md.getParticipants()) {
                final long userId = p.getUserID();
                final long userBank = tRepo.sumAllTransactionsForUserInGuild(userId, guild.getLongID());
                final long userTakeDelta = userBank < cost ? -userBank : -cost;

                takeTransactions.add(new Transaction(userId, guild.getLongID(), userTakeDelta, TransactionType.MONEYDOWN));
                if (winners.contains(userId)) {
                    rewardTransactions.add(new Transaction(userId, guild.getLongID(), perUserWinning, TransactionType.MONEYDOWN));
                }

                if (userBank - userTakeDelta <= 0) {
                    trashed.add(userId);
                }
            }

            for (int i = 0; i < 3; i++) {
                try {
                    md = mdRepo.findOne(betId);
                    md.setResolved(true);
                    md.setWinningUserId(winnerId > -1 ? winnerId : null);
                    md.setSuccess("SUCCESS".equals(passFail) || "FAILURE".equals(passFail) ? "SUCCESS".equals(passFail) : null);
                    mdRepo.save(md);
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

            tRepo.save(takeTransactions);
            tRepo.save(rewardTransactions);

            StringBuilder s = new StringBuilder();

            s.append("\nThe results are in!");
            s.append("\n").append(md.getDescription());
            if (winners.isEmpty()) {
                //EVERYONE IS TRASH
                if (md.getParticipants().isEmpty()) {
                    s.append("\nTHE WHOLE SERVER IS TRASH SINCE YOU DIDN'T CANCEL A BET THAT NO ONE WAS IN!");
                    s.append("\nPretend I posted a picture of the server logo in a burning dumpster, that feature's coming soon");
                    //TODO: trash the server logo
                } else {
                    s.append("\nWAS FAILED BY EVERY PARTICIPANT");
                    s.append("\nWOW");
                    s.append("\n");
                    for (int i =  0; i < md.getParticipants().size(); i++) {
                        s.append("<@").append(md.getParticipants().get(i).getUserID()).append(">");
                        if (i < md.getParticipants().size() - 1) {
                            s.append(" and ");
                        }
                    }
                    s.append("\nYOU LOSE!");
                    s.append("\nPretend I posted a picture of your avatar");
                    if (md.getParticipants().size() != 1) {
                        s.append("s");
                    }
                    s.append(" in a random burning dumpster, that feature's coming soon.");
                    //TODO: trash the user avatar
                }
            } else {
                if (md.getWinningUserId() == null) {
                    if (md.getSuccess()) {
                        s.append("\nHas ended in success!");
                    } else {
                        s.append("\nHas ended in failure!");
                    }
                } else {
                    s.append("\nWas won by ")
                            .append(client.getUserByID(winners.get(0)));
                }

                if (!trashed.isEmpty()) {
                    s.append("\nNobody ended up in the trash. Disappointing.");
                    s.append("\nPretend there isn't a picture of everyone in a dumpster here.");
                } else {

                }

                if (remainderPot > 0) {
                    s.append("\nFinally, the remainder of ")
                            .append(remainderPot)
                            .append(" vanishes into the ether. Yay deflation!");
                    //TODO: make a place to store remainder to pay out with next bet
                }
            }

            FireAndForget.sendMessage(channel, s.toString());

        } catch (DataAccessException ex) {
            FireAndForget.sendMessage(channel, "Could not access the database. Let the bot maintainer know about this.");
            throw ex;
        }
    }
}
