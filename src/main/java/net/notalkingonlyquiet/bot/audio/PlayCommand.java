/*
 * To change this license header, choose License Headers in Project Properties.
 * To change this template file, choose Tools | Templates
 * and open the template in the editor.
 */
package net.notalkingonlyquiet.bot.audio;

import com.sedmelluq.discord.lavaplayer.player.AudioLoadResultHandler;
import com.sedmelluq.discord.lavaplayer.tools.FriendlyException;
import com.sedmelluq.discord.lavaplayer.track.AudioPlaylist;
import com.sedmelluq.discord.lavaplayer.track.AudioTrack;
import net.notalkingonlyquiet.bot.application.RootCommand;
import net.notalkingonlyquiet.bot.util.FireAndForget;
import net.notalkingonlyquiet.bot.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.handle.impl.events.guild.channel.message.MessageReceivedEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;

import java.io.IOException;
import java.net.MalformedURLException;
import java.net.URL;
import java.util.Arrays;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import java.util.stream.Collectors;

/**
 *
 * @author arawson
 */
@Component
final class PlayCommand implements RootCommand, AudioPlayer {

    @Autowired
    private YouTubeSearcher searcher;

    @Autowired
    private LavaAudioManager lam;


    @Override
    public String getName() {
        return "play";
    }

    @Override
    public String getDescription() {
        return "Play something from the internet, or search for it on YouTube if the first argument is not a valid URL";
    }

    @Override
    public void execute(List<String> args, MessageReceivedEvent event) throws RateLimitException, DiscordException, MissingPermissionsException {
        final IChannel channel = event.getChannel();
        final IUser user = event.getAuthor();

        LogUtil.logInfo("playUrl command");
        if (args.size() == 0) {
            FireAndForget.sendMessage(channel, "You must give me a URL or search term as the first argument to that command.");

        } else {
            URL u1 = null;
            try {
                u1 = new URL(args.get(0));
            } catch (MalformedURLException ex) {
            }

            if (u1 == null) {
                try {
                    u1 = searcher.performSearch(Arrays.asList(args).stream().map(Object::toString).collect(Collectors.joining(" ")));
                    if (u1 != null) {
                        FireAndForget.sendMessage(channel, "Playing: " + u1.toString());
                    }
                } catch (IOException ex) {
                    FireAndForget.sendMessage(channel, "Unable to search YouTube. Tell the guy running the bot about this.");
                    Logger.getLogger(getClass().getName()).log(Level.SEVERE, "Unable to search YouTube.", ex);
                }
            }

            if (u1 == null) {
                FireAndForget.sendMessage(channel, "Either the URL is invalid, or the search did not turn up anything.");
                throw new IllegalArgumentException("Either the URL is invalid, or the search did not turn up anything.");
            } else {
                final URL url = u1;
                queue(url, channel, user);
            }
        }
    }

    @Override
    public void queue(URL url, IChannel channel, IUser user) {
        //TODO: decouple joining users channel so we don't need the user parameter
        if (!lam.joinUsersAudioChannel(channel, user)) {
            return;
        }
        LavaAudioManager.GuildMusicManager musicManager = lam.getGuildMusicManager(channel.getGuild());
        lam.getAudioPlayerManager().loadItemOrdered(musicManager, url.toString(), new AudioLoadResultHandler() {
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
