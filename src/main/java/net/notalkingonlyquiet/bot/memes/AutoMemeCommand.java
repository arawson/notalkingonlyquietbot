package net.notalkingonlyquiet.bot.memes;

import net.notalkingonlyquiet.bot.audio.AudioPlayer;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

@Component
public class AutoMemeCommand implements MemeSubCommand {
    @Autowired
    private AudioPlayer player;

    @Autowired
    private MemeRepository memeRepo;

    @Override
    public String getName() {
        return "auto";
    }

    @Override
    public String getDescription() {
        return "Use meme without a command and play a random playable meme.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        List<Meme> memes = memeRepo.findByGuildIdAndType(event.getGuild().getLongID(), MemeType.PLAYABLE);

        if (memes.size() == 0) {
            FireAndForget.sendMessage(event.getChannel(),"Looks like the make-a-meme foundation needs more donations! (This server has no memes!)");
        } else {
            Meme m = memes.get((int)(Math.random() * memes.size()));

            try {
                URL url = new URL(m.getValue());
                player.queue(url, event.getChannel(), event.getAuthor());
            } catch (MalformedURLException ex) {
                FireAndForget.sendMessage(event.getChannel(), "Meme #" + m.getID() + " is set as playable, but has a bad URL. Get that out of here!");
            }
        }
    }
}
