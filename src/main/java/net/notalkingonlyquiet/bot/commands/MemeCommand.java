
package net.notalkingonlyquiet.bot.commands;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.Map;
import net.notalkingonlyquiet.bot.Bot;
import net.notalkingonlyquiet.bot.core.BotService;
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
public final class MemeCommand implements Command {
    private final BotService bot;
    private final MemeManager manager;

    public MemeCommand(BotService bot, MemeManager manager) {
        this.bot = bot;
        this.manager = manager;
    }

    @Override
    public String getBase() {
        return "meme";
    }

    @Override
    public void execute(String[] args, IChannel channel, IUser u) throws RateLimitException, DiscordException, MissingPermissionsException {
        Map<MemeMap.Type, ArrayList<String>> memes = new HashMap<>(manager.getMemeMap().getMemes(channel.getGuild()));
        
        if (memes == null || memes.isEmpty()) {
            bot.sendMessage(channel, "There aren't any memes for this server yet.");
            throw new IllegalArgumentException("There aren't any memes for this server yet.");
        }
        
        ArrayList<String> playables = memes.get(MemeMap.Type.PLAYABLE);
        ArrayList<String> images = memes.get(MemeMap.Type.IMAGE);
        
        if (playables.size() + images.size() == 0) {
            bot.sendMessage(channel, "There aren't any memes for this server yet.");
            throw new IllegalArgumentException("There aren't any memes for this server yet.");
        }
        
        
        MemeMap.Type type = null;
        if (args.length > 0) {
            type = MemeMap.Type.get(args[0]);
            if (type == null) {
                bot.sendMessage(channel, "What type of meme is that? I don't know.");
                throw new IllegalArgumentException("What type of meme is that? I don't know.");
            }
        } else {
            type = MemeMap.Type.random();
        }
        
        ArrayList<String> now = null;
        switch (type) {
            case IMAGE:
                now = images;
                break;
            case PLAYABLE:
                now = playables;
                break;
        }
        
        if (now.isEmpty()) {
            bot.sendMessage(channel, "Looks like the make-a-meme foundation needs more donations!");
            throw new IllegalArgumentException("Yarrrrrrr");
        }
        
        String item = now.get((int)(Math.random() * now.size()));
        
        switch (type) {
            case IMAGE:
                bot.sendMessage(channel, item);
                break;
            case PLAYABLE:
                //TODO: reroute internal commands
                //bot.internalCommand("play", new String[]{item}, channel, u);
                break;
        }
    }
    
}
