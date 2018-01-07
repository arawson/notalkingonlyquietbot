
package net.notalkingonlyquiet.bot.memes;

import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Bean;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.net.MalformedURLException;
import java.net.URL;
import java.util.List;

/**
 *
 * @author arawson
 */

@Component
final class AddMemeCommand implements MemeSubCommand {
    @Autowired
    private MemeRepository memeRepo;

    private static final String help =
            "meme add help -> display this help message\n" +
            "meme add <TYPE> <URL> -> add a meme to the database\n" +
            "    <TYPE> must be one of TEXT or PLAYABLE\n" +
            "    PLAYABLE memes should be something with some sound to play in a voice channel\n" +
            "    TEXT memes should be anything else, from copypasta to image URLs\n" +
            "    <URL> must be a valid URL";

    @Override
    public String getName() {
        return "add";
    }

    @Override
    public String getDescription() {
        return "Add memes to the meme database. MEME ADD for usage.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final long guildId = event.getGuild().getLongID();

        if (args.size() >= 1) {
            if (args.get(0).toUpperCase() == "HELP") {printHelp(event.getChannel());}
            else {
                MemeType type = null;
                switch (args.get(0).toUpperCase()) {
                    case "PLAYABLE": {
                        type = MemeType.PLAYABLE;
                        if (args.size() < 2) {
                            FireAndForget.sendMessage(event.getChannel(), "There must be a URL to add!");
                        } else {
                            try {
                                URL url = new URL(args.get(1));
                                String urlString = url.toString();
                                long existing = memeRepo.countByGuildIdAndTypeAndValue(guildId, type, urlString);

                                if (existing < 1) {
                                    memeRepo.save(new Meme(guildId, type, urlString));
                                    FireAndForget.sendMessage(event.getChannel(), "New meme added!");
                                } else {
                                    FireAndForget.sendMessage(event.getChannel(), "That meme is already registered.");
                                }
                            } catch (MalformedURLException ex) {
                                FireAndForget.sendMessage(event.getChannel(), "That is not a valid URL! " + args.get(1));
                            }
                        }
                        break;
                    }
                    case "TEXT": {
                        type = MemeType.TEXT;
                        if (args.size() < 2) {
                            FireAndForget.sendMessage(event.getChannel(), "There must be some text to add!");
                        } else {
                            String value = String.join(" ", args.subList(1, args.size()));
                            long existing = memeRepo.countByGuildIdAndTypeAndValue(guildId, type, value);

                            if (existing < 1) {
                                memeRepo.save(new Meme(guildId, type, value));
                                FireAndForget.sendMessage(event.getChannel(), "New meme added!");
                            } else {
                                FireAndForget.sendMessage(event.getChannel(), "That meme is already there!");
                            }
                        }
                        break;
                    }
                    default: {
                        FireAndForget.sendMessage(event.getChannel(), "What type of meme is that? " + args.get(0));
                    }
                }
            }
        } else {
            printHelp(event.getChannel());
        }
    }

    private void printHelp(IChannel channel) {
        FireAndForget.sendMessage(channel, help);
    }
}
