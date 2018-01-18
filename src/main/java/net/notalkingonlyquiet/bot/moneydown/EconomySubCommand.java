package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.CommandUtil;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.text.DecimalFormat;
import java.text.Format;
import java.util.List;

@Component
public class EconomySubCommand implements MoneyDownSubCommand {
    private static final Format formatter = new DecimalFormat("#.00");

    @Autowired
    private TransactionRepository tRepo;

    @Autowired
    private MoneyDownRepository mdRepo;

    @Override
    public String getName() {
        return "ECONOMY";
    }

    @Override
    public String getDescription() {
        return "Find out the current state of the economy!";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IGuild guild = event.getGuild();
        final IChannel channel = event.getChannel();

        final long economy = tRepo.sumAllTransactionsForGuild(guild.getLongID());
        final long finishedBets = mdRepo.countByGuildIDAndResolvedAndCancelled(guild.getLongID(), true, false);
        final long initialEconomy = tRepo.sumAllTransactionsForGuildByType(guild.getLongID(), TransactionType.PLACEMENT);

        StringBuilder s = new StringBuilder();

        s.append("\nThis is Meme McCream with your quarterly economic report!");
        s.append("\nThe ")
                .append(guild.getName())
                .append(" GDP has risen to ")
                .append(economy)
                .append(" money from its initial size of ")
                .append(initialEconomy)
                .append(" money.");

        s.append("\n")
                .append(finishedBets)
                .append(" bets have been completed.");

        if (initialEconomy > 0) {
            final double inflation = ((double)economy / (double)initialEconomy) - 1.0;
            s.append("\nInflation has risen to ")
                    .append(formatter.format(inflation * 100))
                    .append("%");
            if (inflation > 100.00 && CommandUtil.chance(0.5)) {
                s.append(" under the guidance of our glorious leader.");
            }

            if (finishedBets > 0) {
                final double inflationPerBet = inflation / finishedBets;
                s.append("\nAverage inflation per bet is ")
                        .append(formatter.format(inflationPerBet * 100))
                        .append("%.");
            }
        }

        s.append("\nThis has been your economic growth report with Meme McCream.");

        FireAndForget.sendMessage(channel, s.toString());
    }
}
