
package net.notalkingonlyquiet.bot.commands;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;
import net.notalkingonlyquiet.bot.Bot;
import net.notalkingonlyquiet.bot.FireAndForget;
import net.notalkingonlyquiet.bot.LogUtil;
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
public final class PlayCommand implements Command {
    
    private final Bot outer;

    public PlayCommand(final Bot outer) {
        this.outer = outer;
    }

    @Override
    public String getBase() {
        return "play";
    }

    @Override
    public void execute(String[] args, IChannel channel, IUser user) throws RateLimitException, DiscordException, MissingPermissionsException {
        LogUtil.logInfo("playUrl command");
        if (args.length == 0) {
            channel.sendMessage("You must give me a URL as the first argument to that command.");
            throw new IllegalArgumentException("The play command requires at least one argument.");
        }
        URL u1 = null;
        try {
            u1 = new URL(args[0]);
        } catch (MalformedURLException ex) {
        }
        //TODO: insert youtube search here
        if (u1 == null) {
            try {
                u1 = outer.getYouTubeSearcher().performSearch(Arrays.asList(args).stream().map(Object::toString).collect(Collectors.joining(" ")));
                if (u1 != null) {
                    FireAndForget.sendMessage(channel, "Playing: " + u1.toString());
                }
            } catch (IOException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (u1 == null) {
            FireAndForget.sendMessage(channel, "Either the URL is invalid, or the search did not turn up anything.");
            throw new IllegalArgumentException("Either the URL is invalid, or the search did not turn up anything.");
        }
        final URL url = u1;
        if (!outer.joinUsersAudioChannel(channel, user)) {
            return;
        }
        GuildMusicManager musicManager = outer.getGuildMusicManager(channel.getGuild());
        outer.getAudioPlayerManager().loadItemOrdered(musicManager, u1.toString(), new AudioLoadResultHandler() {
            @Override
            public void trackLoaded(AudioTrack track) {
                LogUtil.logInfo("track " + track.getIdentifier() + " loaded");
                FireAndForget.sendMessage(channel, "Track loaded.");
                //TODO: setTrackTitle(getPlayer(channel.getGuild()).queue(u), u.getFile());
                musicManager.userQueue(channel, user, track);
            }

            @Override
            public void playlistLoaded(AudioPlaylist playlist) {
                LogUtil.logInfo("playlist loaded " + playlist.getName());
                AudioTrack firstTrack = playlist.getSelectedTrack();
                if (firstTrack == null) {
                    firstTrack = playlist.getTracks().get(0);
                }
                FireAndForget.sendMessage(channel, "Adding to queue " + firstTrack.getInfo().title + " (first track of playlist " + playlist.getName() + ")");
                musicManager.userQueue(channel, user, firstTrack);
                //TODO: setTrackTitle(getPlayer(channel.getGuild()).queue(u), u.getFile());
            }

            @Override
            public void noMatches() {
                FireAndForget.sendMessage(channel, "Could not play " + url.toString());
            }

            @Override
            public void loadFailed(FriendlyException fe) {
                FireAndForget.sendMessage(channel, "Could not find anything at " + url.toString());
            }
        });
    }
    
}
