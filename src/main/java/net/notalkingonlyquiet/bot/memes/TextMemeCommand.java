package net.notalkingonlyquiet.bot.memes;

import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

@Component
public class TextMemeCommand implements MemeSubCommand {
    @Autowired
    private MemeRepository memeRepo;

    @Override
    public String getName() {
        return "text";
    }

    @Override
    public String getDescription() {
        return "Spit out a non-playable meme into chat and annoy everyone!";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        List<Meme> memes = memeRepo.findByGuildIDAndType(event.getGuild().getLongID(), MemeType.TEXT);

        if (memes.size() == 0) {
            FireAndForget.sendMessage(event.getChannel(),"Looks like the make-a-meme foundation needs more donations! (This server has no memes!)");
        } else {
            Meme m = memes.get((int)(Math.random() * memes.size()));
            FireAndForget.sendMessage(event.getChannel(), m.getValue());
        }
    }
}
