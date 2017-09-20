
package net.notalkingonlyquiet.bot.commands;

import net.notalkingonlyquiet.bot.Bot;
import net.notalkingonlyquiet.bot.audio.AudioService;
import net.notalkingonlyquiet.bot.audio.GuildMusicManager;
import net.notalkingonlyquiet.bot.core.BotService;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
//TODO: make this the skip command on the audio module 
public final class SkipCommand implements Command {
    
    private final BotService bot;
    private final AudioService audioService;

    public SkipCommand(BotService outer, AudioService audioService) {
        this.bot = outer;
        this.audioService = audioService;
    }

    @Override
    public String getBase() {
        return "skip";
    }

    @Override
    public void execute(String[] args, IChannel channel, IUser u) throws RateLimitException, DiscordException, MissingPermissionsException {
        GuildMusicManager manager = audioService.getGuildMusicManager(channel.getGuild());
        manager.nextTrack();
        bot.sendMessage(channel, "Skipping to next track.");
    }
    
}
