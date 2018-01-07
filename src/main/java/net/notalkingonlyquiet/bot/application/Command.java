
package net.notalkingonlyquiet.bot.application;

import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

/**
 *
 * @author arawson
 */
public interface Command {
    public String getName();

    public String getDescription();
    
    public void execute(List<String> args, MessageReceivedEvent event) throws
            RateLimitException, DiscordException, MissingPermissionsException;
}
