package net.notalkingonlyquiet.bot.moneydown;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import java.sql.Timestamp;
import java.util.List;

/**
 * Provides various utilities that the moneydown commands commonly need.
 */
public class Util {
    //TODO: load from config file or inject
    public static final long STARTING_MONEY = 50;

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
                Transaction t = new Transaction(u, guild, STARTING_MONEY, TransactionType.PLACEMENT);

                repo.save(t);
            }
        }
    }

    static void initSingleUserOnGuild(IUser user, IGuild guild, TransactionRepository repo) {
        if (!user.isBot() &&
                repo.countByGuildIDAndUserIDAndType(guild.getLongID(), user.getLongID(), TransactionType.PLACEMENT) == 0) {
            Transaction t = new Transaction(user, guild, STARTING_MONEY, TransactionType.PLACEMENT);

            repo.save(t);
        }
    }

    static long getUserBalanceInGuild(IUser user, IGuild guild, TransactionRepository repo) {
        if (user.isBot()) {
            throw new UnsupportedOperationException("Cannot add user as they are a bot!");
        } else {
            initSingleUserOnGuild(user, guild, repo);

            return repo.sumAllTransactionsForUserInGuild(user.getLongID(), guild.getLongID());
        }
    }

    static Timestamp getNow() {
        return new Timestamp(System.currentTimeMillis());
    }
}
