/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
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
final class SkipCommand implements Command {
    
    private final Bot outer;

    SkipCommand(final Bot outer) {
        this.outer = outer;
    }

    @Override
    public String getBase() {
        return "skip";
    }

    @Override
    public void execute(String[] args, IChannel channel, IUser u) throws RateLimitException, DiscordException, MissingPermissionsException {
        GuildMusicManager manager = outer.getGuildMusicManager(channel.getGuild());
        manager.nextTrack();
        FireAndForget.sendMessage(channel, "Skipping to next track.");
    }
    
}
