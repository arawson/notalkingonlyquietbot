
package net.notalkingonlyquiet.bot;

import net.notalkingonlyquiet.bot.audio.GuildMusicManager;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
final class VolumeCommand implements Command {
    
    private final Bot outer;

    VolumeCommand(Bot outer) {
        this.outer = outer;
    }

    @Override
    public String getBase() {
        return "volume";
    }

    @Override
    public void execute(String[] args, IChannel channel, IUser u) throws RateLimitException, DiscordException, MissingPermissionsException {
        if (args.length == 0) {
            FireAndForget.sendMessage(channel, "You must give me a volume level to set.");
            throw new IllegalArgumentException("The volume command requries a level argument.");
        }
        
        try {
            int level = Integer.parseInt(args[0]);
            
            if (level < 0 || level > 100) {
                throw new IllegalArgumentException("The volume must be between 0 and 100");
            }
            
            GuildMusicManager musicManager = outer.getGuildMusicManager(channel.getGuild());
            musicManager.setVolume(level);
            
            FireAndForget.sendMessage(channel, "Set volume to " + level + ".");
        } catch (NumberFormatException e) {
            FireAndForget.sendMessage(channel, "That is not a valid integer."
                    + " How the heck am I supposed to set audio to " + args[0] + "?");
            throw e;
        }
    }
    
}
