package net.notalkingonlyquiet.bot.memes;

import org.springframework.data.repository.CrudRepository;
import org.springframework.stereotype.Repository;

import java.util.List;

@Repository
public interface MemeRepository extends CrudRepository<Meme, Long> {

    List<Meme> findByGuildIdAndType(Long guildId, MemeType type);

    long countByGuildIdAndTypeAndValue(Long guildId, MemeType type, String value);

    long countByGuildIdAndType(Long guildId, MemeType type);
}
