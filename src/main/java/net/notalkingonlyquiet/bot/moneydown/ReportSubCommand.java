package net.notalkingonlyquiet.bot.moneydown;

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
public class ReportSubCommand implements MoneyDownSubCommand {
    @Autowired
    private MoneyDownRepository mdRepo;

    @Override
    public String getName() {
        return "REPORT";
    }

    @Override
    public String getDescription() {
        return "Reports the status of all open moneydown situations!";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IDiscordClient client = event.getClient();
        final IGuild guild = event.getGuild();
        final IChannel channel = event.getChannel();

        StringBuilder s = new StringBuilder();

        List<MoneyDown> m = mdRepo.findByGuildIDAndResolved(guild.getLongID(), false);

        if (m.isEmpty()) {
            FireAndForget.sendMessage(channel, "There no open bets right now!");
        } else {
            s.append("\nThese are the current open moneydowns:");

            try {

                for (MoneyDown md : m) {
                    s.append("\n#")
                            .append(md.getId())
                            .append(" ")
                            .append(md.getType() == BetType.FFA ? "Free-for-All " : "For ")
                            .append(md.getEntryCost())
                            .append(" money: \"")
                            .append(md.getDescription())
                            .append("\"");

                    if (!md.getParticipants().isEmpty()) {
                        s.append(" and these users are in: ");
                        for (Participant p : md.getParticipants()) {
                            IUser u = client.getUserByID(p.getUserID());
                            s.append("(");
                            s.append(u.getName());

                            if (md.getType() == BetType.BET) {
                                s.append(" is ")
                                        .append(p.isInAgreement() ? "for" : "against");
                            }
                            s.append(") ");
                        }

                        long pot = md.getEntryCost() * md.getParticipants().size();
                        s.append("that means the total pot is ")
                                .append(pot)
                                .append(" money");

                        if (pot < 250) {
                        } else if (pot < 500) {
                            s.append(" *THAT'S A LOT OF DAMAGE!*");
                        } else if (pot < 1000) {
                            s.append(" *WE GOT A SHOOTER AND A GOOD'EN!*");
                        } else {
                            s.append(" *WHO WANTS, WHO WANTS THE HARD FOUR?*");
                        }
                    }
                }
            } catch (DataAccessException ex) {
                s.append("\nWHAT? WHERE ARE THEY?!?!?!");
                s.append("\nTell the bot maintainer about this.");
                throw ex;
            } finally {
                FireAndForget.sendMessage(channel, s.toString());
            }
        }
    }
}
