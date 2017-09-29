
package net.notalkingonlyquiet.bot.core;

import net.notalkingonlyquiet.bot.audio.CantJoinAudioChannelException;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;

/**
 *
 * @author arawson
 */
public interface BotService {
    
    public IVoiceChannel joinUsersAudioChannel(IGuild guild, IUser user) throws CantJoinAudioChannelException;
    
    public void joinVoice(IVoiceChannel channel);
    
    public void sendMessage(IChannel channel, String message);
}
