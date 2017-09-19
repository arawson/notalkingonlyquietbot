
package net.notalkingonlyquiet.bot.commands;

import java.net.MalformedURLException;
import java.net.URL;
import net.notalkingonlyquiet.bot.Bot;
import net.notalkingonlyquiet.bot.FireAndForget;
import net.notalkingonlyquiet.bot.fun.MemeMap;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
public final class AddMemeCommand implements Command {
    private final Bot bot;
    private final MemeManager manager;

    //TODO: convert to dependancy injection
    public AddMemeCommand(Bot bot, MemeManager manager) {
        this.bot = bot;
        this.manager = manager;
    }

    @Override
    public String getBase() {
        return "addmeme";
    }

    @Override
    public void execute(String[] args, IChannel channel, IUser u) throws RateLimitException, DiscordException, MissingPermissionsException {
        if (args.length < 2) {
            FireAndForget.sendMessage(channel, "This command requires 2 arguments, the type (play or image) and the link.");
            throw new IllegalArgumentException("This command requires 2 arguments");
        }
        
        String typeArg = args[0];
        MemeMap.Type type;
        //TODO: make the meme map handle meme type management
        switch (typeArg) {
            case "play":
                type = MemeMap.Type.PLAYABLE;
                break;
            case "image":
                type = MemeMap.Type.IMAGE;
                break;
            default:
                FireAndForget.sendMessage(channel, "That is not a type of meme that I know: " + typeArg + ".");
                throw new IllegalArgumentException("That is not a type of meme that I know: " + typeArg + ".");
        }
        
        String link = args[1];
        try {
            URL url = new URL(link);
        } catch (MalformedURLException ex) {
            FireAndForget.sendMessage(channel, "I only accept URLs for the make-a-meme foundation.");
            throw new IllegalArgumentException("I only accept URLs for the make-a-meme foundation.", ex);
        }
        
        manager.getMemeMap().putMeme(channel.getGuild(), type, link);
        FireAndForget.sendMessage(channel, "Meme added.");
        manager.save();
    }
    
}
