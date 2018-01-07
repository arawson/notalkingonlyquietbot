package net.notalkingonlyquiet.bot.audio;

import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;

import java.net.URL;

public interface AudioPlayer {
    public void queue(URL url, IChannel channel, IUser user);
}
