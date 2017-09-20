package net.notalkingonlyquiet.bot.commands;

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
public final class VolumeCommand implements Command {

    private final BotService outer;
    private final AudioService audioService;

    public VolumeCommand(BotService outer, AudioService audioService) {
        this.outer = outer;
        this.audioService = audioService;
    }

    @Override
    public String getBase() {
        return "volume";
    }

    @Override
    public void execute(String[] args, IChannel channel, IUser u)
            throws RateLimitException, DiscordException, MissingPermissionsException {
        if (args.length == 0) {
            outer.sendMessage(channel, "You must give me a volume level to set.");
            throw new IllegalArgumentException("The volume command requries a level argument.");
        }

        try {
            int level = Integer.parseInt(args[0]);

            if (level < 0 || level > 100) {
                outer.sendMessage(channel, "The volume must be between 0 and 100");
                throw new IllegalArgumentException("The volume must be between 0 and 100");
            }

            GuildMusicManager musicManager = audioService.getGuildMusicManager(channel.getGuild());
            musicManager.setVolume(level);

            outer.sendMessage(channel, "Set volume to " + level + ".");
        } catch (NumberFormatException e) {
            outer.sendMessage(channel, "That is not a valid integer."
                    + " How the heck am I supposed to set audio to " + args[0] + "?");
            throw e;
        }
    }

}
