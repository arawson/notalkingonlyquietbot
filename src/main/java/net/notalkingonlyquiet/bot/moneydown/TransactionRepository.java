package net.notalkingonlyquiet.bot.moneydown;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;
import org.springframework.data.repository.query.Param;

public interface TransactionRepository extends CrudRepository<MoneyDownTransaction, Long> {
    @Query(" select sum(amount) from MoneyDownTransaction where userID = :userID and guildID = :guildID")
    public long sumAllTransactionsForUserInGuild(
            @Param("userID") long userID,
            @Param("guildID") long guildID
    );

    public long countByGuildIDAndUserID(long userID, long guildID);
}
