package net.notalkingonlyquiet.bot.memes;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemeRepository extends CrudRepository<Meme, Long> {

    List<Meme> findByGuildIDAndType(Long guildId, MemeType type);

    long countByGuildIDAndTypeAndValue(Long guildId, MemeType type, String value);

    long countByGuildIDAndType(Long guildId, MemeType type);
}
