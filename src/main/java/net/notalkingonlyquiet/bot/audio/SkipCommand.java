/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
final class SkipCommand implements RootCommand {

    @Autowired
    private LavaAudioManager lam;

    @Override
    public String getName() {
        return "skip";
    }

    @Override
    public String getDescription() {
        return "Skips the currently playing audio track.";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IChannel channel = event.getChannel();

        LavaAudioManager.GuildMusicManager mm = lam.getGuildMusicManager(channel.getGuild());
        mm.nextTrack();
        FireAndForget.sendMessage(channel, "Skipping to next track.");
    }
}
