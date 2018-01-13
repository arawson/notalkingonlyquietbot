package net.notalkingonlyquiet.bot.moneydown;

import org.hibernate.Transaction;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.util.List;

/**
 * Provides various utilities that the moneydown commands commonly need.
 */
public class Util {
    //TODO: load from config file or inject
    private static long STARTING_MONEY = 50;

    /**
     * Make sure each user on the guild has an initial transaction.
     *
     * @param guild
     * @param repo
     */
    static void initUsersOnGuild(IGuild guild, TransactionRepository repo) {
        List<IUser> users = guild.getUsers();

        for (IUser u : users) {
            if (u.isBot()) {
                continue;
            }
            if (repo.countByGuildIDAndUserIDAndType(guild.getLongID(), u.getLongID(), TransactionType.PLACEMENT) == 0) {
                MoneyDownTransaction t = new MoneyDownTransaction(
                        u.getLongID(), guild.getLongID(), STARTING_MONEY, TransactionType.PLACEMENT);

                repo.save(t);
            }
        }
    }
}
