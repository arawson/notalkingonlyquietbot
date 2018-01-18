package net.notalkingonlyquiet.bot.moneydown;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends CrudRepository<Transaction, Long> {
    @Query(" select sum(amount) from Transaction where userID = :userID and guildID = :guildID")
    public long sumAllTransactionsForUserInGuild(
            @Param("userID") long userID,
            @Param("guildID") long guildID
    );

    @Query("select sum(amount) from Transaction where guildID = :guildID")
    public long sumAllTransactionsForGuild(
            @Param("guildID") long guildID
    );

    @Query("select sum(amount) from Transaction where guildID = :guildID and type = :type")
    public long sumAllTransactionsForGuildByType(
            @Param("guildID") long guildID,
            @Param("type") TransactionType type
    );

    public long countByGuildIDAndUserID(long userID, long guildID);

    public long countByGuildIDAndUserIDAndType(long userId, long guildID, TransactionType type);
}
