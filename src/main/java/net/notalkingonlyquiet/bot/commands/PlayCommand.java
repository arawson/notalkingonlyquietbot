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
import net.notalkingonlyquiet.bot.LogUtil;
import net.notalkingonlyquiet.bot.audio.AudioService;
import net.notalkingonlyquiet.bot.audio.CantJoinAudioChannelException;
import net.notalkingonlyquiet.bot.audio.GuildMusicManager;
import net.notalkingonlyquiet.bot.core.BotService;
import net.notalkingonlyquiet.bot.googlesearch.YouTubeSearcher;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

/**
 *
 * @author arawson
 */
public final class PlayCommand implements Command {

    private final BotService outer;
    private final AudioService audioService;
    private final YouTubeSearcher searcher;

    public PlayCommand(BotService outer, AudioService audioService, YouTubeSearcher searcher) {
        this.outer = outer;
        this.searcher = searcher;
        this.audioService = audioService;
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
                //TODO: make a thing 
                u1 = searcher.performSearch(Arrays.asList(args).stream().map(Object::toString).collect(Collectors.joining(" ")));
                if (u1 != null) {
                    outer.sendMessage(channel, "Playing: " + u1.toString());
                }
            } catch (IOException ex) {
                Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
        if (u1 == null) {
            outer.sendMessage(channel, "Either the URL is invalid, or the search did not turn up anything.");
            throw new IllegalArgumentException("Either the URL is invalid, or the search did not turn up anything.");
        }
        final URL url = u1;

        IVoiceChannel voice = null;
        try {
            voice = outer.joinUsersAudioChannel(channel.getGuild(), user);
        } catch (CantJoinAudioChannelException ex) {
            Logger.getLogger(PlayCommand.class.getName()).log(Level.SEVERE, null, ex);
            outer.sendMessage(channel, ex.getMessage());
        }

        if (voice == null) {
            return;
        }

        GuildMusicManager musicManager = audioService.getGuildMusicManager(channel.getGuild());
        musicManager.playURL(user, url, new GuildMusicManager.PlayFeedbackListener() {
            @Override
            public void trackLoaded(String identifier) {
                //TODO: setTrackTitle(getPlayer(channel.getGuild()).queue(u), u.getFile());
                LogUtil.logInfo("track " + identifier + " loaded");
                //outer.sendMessage(channel, "Track loaded.");
            }

            @Override
            public void playlistLoaded(String name) {
                LogUtil.logInfo("playlist loaded " + name);
                //outer.sendMessage(channel, "Adding to queue "
                        //+ firstTrack.getInfo().title + " (first track of playlist "
                        //+ playlist.getName() + ")");
                //TODO: setTrackTitle(getPlayer(channel.getGuild()).queue(u), u.getFile());
            }

            @Override
            public void noMatches() {
                outer.sendMessage(channel, "Could not play " + url.toString());
            }

            @Override
            public void LoadFailed(Exception e) {
                outer.sendMessage(channel, "Could not find anything at " + url.toString());
            }
        });
    }

}
