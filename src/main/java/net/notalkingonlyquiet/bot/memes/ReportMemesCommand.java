package net.notalkingonlyquiet.bot.memes;

import net.notalkingonlyquiet.bot.util.FireAndForget;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.util.List;

@Component
public class ReportMemesCommand implements MemeSubCommand {
    @Autowired
    private MemeRepository memeRepo;

    @Override
    public String getName() {
        return "report";
    }

    @Override
    public String getDescription() {
        return "Reports how many memes are saved for this server.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final StringBuilder report = new StringBuilder();
        final long guildId = event.getGuild().getLongID();

        report.append("This is your top-of-the-hour meme report!\n");
        report.append("I'm Meme McCream and these are the results of the latest meme census:\n");
        for (MemeType type : MemeType.values()) {
            report.append(type.toString());
            report.append(" -> ");
            report.append(memeRepo.countByGuildIDAndType(guildId, type));
            report.append("\n");
        }
        report.append("This has been Meme McCream and your top-of-the-hour meme news.");

        FireAndForget.sendMessage(event.getChannel(), report.toString());
    }
}
