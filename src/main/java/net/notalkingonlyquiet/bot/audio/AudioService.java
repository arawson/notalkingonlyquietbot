package net.notalkingonlyquiet.bot.audio;

import sx.blah.discord.handle.obj.IGuild;

/**
 *
 * @author arawson
 */
public interface AudioService {

    public GuildMusicManager getGuildMusicManager(IGuild guild);
}
