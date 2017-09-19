
package net.notalkingonlyquiet.bot.commands;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
public interface Command {
    public String getBase();
    
    public void execute(String[] args, IChannel channel, IUser u) throws
            RateLimitException, DiscordException, MissingPermissionsException;
}
