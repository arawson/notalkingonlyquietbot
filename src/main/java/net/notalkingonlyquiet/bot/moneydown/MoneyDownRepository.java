package net.notalkingonlyquiet.bot.moneydown;

import org.springframework.data.jpa.repository.Query;
import org.springframework.data.repository.CrudRepository;

import java.util.List;

public interface MoneyDownRepository extends CrudRepository<MoneyDown, Long> {

    public List<MoneyDown> findByGuildIDAndResolved(long guildID, boolean resolved);

    public long countByGuildIDAndResolvedAndCancelled(long guildID, boolean resolved, boolean cancelled);
}
