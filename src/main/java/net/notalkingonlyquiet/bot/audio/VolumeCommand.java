
package net.notalkingonlyquiet.bot.audio;

import net.notalkingonlyquiet.bot.application.RootCommand;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

/**
 *
 * @author arawson
 */
@Component
final class VolumeCommand implements RootCommand {

    @Autowired
    private LavaAudioManager lam;

    @Override
    public String getName() {
        return "volume";
    }

    @Override
    public String getDescription() {
        return "Sets the volume.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IChannel channel = event.getChannel();

        if (args.size() == 0) {
            FireAndForget.sendMessage(channel, "You must give me a volume level to set.");
        } else {
            try {
                int level = Integer.parseInt(args.get(0));

                if (level < 0 || level > 100) {
                    FireAndForget.sendMessage(channel, "The volume must be between 0 and 100");
                } else {
                    LavaAudioManager.GuildMusicManager mm = lam.getGuildMusicManager(channel.getGuild());
                    mm.setVolume(level);
                    FireAndForget.sendMessage(channel, "Set volume to " + level + ".");
                }
            } catch (NumberFormatException e) {
                FireAndForget.sendMessage(channel, "That is not a valid integer."
                        + " How the heck am I supposed to set audio to " + args.get(0) + "?");
            }
        }
    }
}
