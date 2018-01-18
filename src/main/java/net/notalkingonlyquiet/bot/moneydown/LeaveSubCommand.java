package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.ArgValidator;
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
public class LeaveSubCommand implements MoneyDownSubCommand {
    private final static String NO_ID_ERR = "NO ID";
    private static final ArgValidator VALIDATOR = new ArgValidator()
            .toUpperCase()
            .expectInt(NO_ID_ERR);

    @Autowired
    private MoneyDownRepository mdRepo;

    @Override
    public String getName() {
        return "LEAVE";
    }

    @Override
    public String getDescription() {
        return "Leave a bet you joined. <.<  moneydown leave <ID>";
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
                    case ArgValidator.EXPECTED_MORE_ERROR:
                        e.append("moneydown leave <ID>");
                        break;
                    case ArgValidator.TOO_MANY_ERROR:
                        e.append("\nToo many arguments.");
                        break;
                }
            }

            FireAndForget.sendMessage(channel, e.toString());
        } else {
            final long id = (Integer)parse.results.get(0);
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
            } else if (m.getParticipants().stream().noneMatch(p -> p.getUserID() == user.getLongID())) {
                FireAndForget.sendMessage(channel, "You are not in this bet.");
            } else if (m.getLastTakeableTime().before(now)) {
                FireAndForget.sendMessage(channel, "It's too late to leave the bet!");
            } else if (m.isCancelled()) {
                FireAndForget.sendMessage(channel, "That bet has been cancelled.");
            } else if (m.isResolved()) {
                FireAndForget.sendMessage(channel, "That bet has already concluded.");
            } else {
                for (int i = 0; i < 3; i++ ) {
                    m = mdRepo.findOne(id);

                    m.getParticipants().removeIf(p -> p.getUserID() == user.getLongID());

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

                FireAndForget.sendMessage(channel, "You have been removed from the bet.");
            }
        }
    }
}
