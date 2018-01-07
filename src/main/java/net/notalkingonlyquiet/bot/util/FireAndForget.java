
package net.notalkingonlyquiet.bot.util;

import com.google.common.base.Preconditions;
import java.util.logging.Level;
import java.util.logging.Logger;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 * Use when you don't care if the operation succeeds.
 * @author arawson
 */
public final class FireAndForget {
    public static final void sendMessage(IChannel channel, String message) {
        LogUtil.logInfo("Sending message on " + channel.getName() + ": " + message);
        try {
            synchronized(channel) {
                channel.sendMessage(message);
            }
        } catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
            Logger.getLogger(FireAndForget.class.getName()).log(Level.SEVERE, null, ex);
        }
    }
    
    public static final void joinVoice(IVoiceChannel channel) {
        try {
            channel.join();
        } catch (MissingPermissionsException ex) {
            Preconditions.checkArgument(false, "Can't join " + channel.getName() + ".");
        }
    }
}
