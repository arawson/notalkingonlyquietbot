package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.ArgValidator;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import net.notalkingonlyquiet.bot.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
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

import javax.persistence.OptimisticLockException;
import java.util.List;
import java.util.stream.Collectors;

@Component
public class CancelSubCommand implements MoneyDownSubCommand {
    private static final String BAD_ID_ERR = "BAD ID";
    private static final ArgValidator VALIDATOR = new ArgValidator()
            .toUpperCase()
            .expectInt(BAD_ID_ERR);
    private static final String HELP = "moneydown cancel <ID>";

    @Autowired
    private MoneyDownRepository mdRepo;

    @Override
    public String getName() {
        return "CANCEL";
    }

    @Override
    public String getDescription() {
        return "Cancels a bet.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IDiscordClient client = event.getClient();
        final IGuild guild = event.getGuild();
        final IUser user = event.getAuthor();
        final IChannel channel = event.getChannel();

        ArgValidator.Result parse = VALIDATOR.parse(args);

        if (!parse.ok) {
            StringBuilder e = new StringBuilder();
            for (String s: parse.errors) {
                switch (s) {
                    case BAD_ID_ERR:
                        e.append("\nYou must enter an ID that is a positive whole number.");
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

            MoneyDown md = mdRepo.findOne(id);

            System.out.println("md is " + md);

            if (md == null || (md.getGuildID() != guild.getLongID())) {
                FireAndForget.sendMessage(channel, "Could not find bet " + id);
            } else {
                //TODO: allow customizable roles (cancelling can only be done by admins right now)
                List<Long> adminRoles =
                        guild.getRoles().stream()
                                .filter(r -> r.getName().toUpperCase().contains("ADMIN"))
                                .map(r -> r.getLongID())
                                .collect(Collectors.toList());

                System.out.println("md is " + md);
                if (user.isBot() || (
                        user.getLongID() != md.getCreatedByUserID()
                        && user.getRolesForGuild(guild).stream().noneMatch(r -> adminRoles.contains(r.getLongID())))
                        ) {
                    FireAndForget.sendMessage(channel, "You must be an admin or the creator of a bet to cancel it.");
                } else if (md.isCancelled()) {
                    FireAndForget.sendMessage(channel, "That bet has been cancelled.");
                } else if (md.isResolved()) {
                    FireAndForget.sendMessage(channel, "That bet has already concluded.");
                } else {
                    for (int i = 0; i < 3; i++) {
                        md = mdRepo.findOne(id);
                        md.setCancelled(true);
                        md.setResolved(true);

                        try {
                            mdRepo.save(md);
                            FireAndForget.sendMessage(channel, "Bet " + id + " cancelled.");
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
                }
            }
        }
    }
}
