package net.notalkingonlyquiet.bot;

import net.notalkingonlyquiet.bot.core.BotService;
import net.notalkingonlyquiet.bot.commands.MemeCommand;
import net.notalkingonlyquiet.bot.commands.Command;
import net.notalkingonlyquiet.bot.commands.MemeManager;
import net.notalkingonlyquiet.bot.commands.AddMemeCommand;
import com.google.common.base.MoreObjects;
import com.google.common.base.Preconditions;
import com.google.inject.Inject;
import com.google.inject.Singleton;
import java.io.IOException;
import java.util.Arrays;
import java.util.HashMap;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import net.notalkingonlyquiet.bot.audio.CantJoinAudioChannelException;
import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.googlesearch.YouTubeSearcher;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.GuildCreateEvent;
import sx.blah.discord.handle.impl.events.MessageReceivedEvent;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.obj.IChannel;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IMessage;
import sx.blah.discord.handle.obj.IUser;
import sx.blah.discord.handle.obj.IVoiceChannel;
import sx.blah.discord.handle.obj.Permissions;
import sx.blah.discord.handle.obj.Status;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.MissingPermissionsException;
import sx.blah.discord.util.RateLimitException;
import net.notalkingonlyquiet.bot.core.ConfigProvider;
import net.notalkingonlyquiet.bot.core.BusProvider;
import net.notalkingonlyquiet.bot.core.ClientProvider;
import net.notalkingonlyquiet.bot.core.events.ClientAbortEvent;
import net.notalkingonlyquiet.bot.core.events.ClientReadyEvent;

/**
 *
 * @author arawson
 */
//TODO: rename/convert this to discord connection manager
@Singleton
public final class Bot implements BotService {

	private final ClientProvider clientProvider;
	private final String prefix;
	private final int maxServers;

	//associate servers to audio channels
	private final Map<IGuild, IChannel> lastChannel = new HashMap<>();
	private final Map<String, Command> commands = new HashMap<>();

	private final MemeManager memeManager;

	private final YouTubeSearcher youTubeSearcher;

	private boolean dead = false;
	private final BusProvider busProvider;

	private final String playing;

	@Inject
	public Bot(ClientProvider cp, ConfigProvider configP, BusProvider bs) throws IOException {
		busProvider = bs;
		clientProvider = cp;

		playing = configP.getConfig().login.playing;

		final Config config = configP.getConfig();

		prefix = MoreObjects.firstNonNull(config.bot.prefix, "!");
		maxServers = config.performance.servers;

		youTubeSearcher = new YouTubeSearcher(config.google);
		memeManager = new MemeManager(config.memes);

		//TODO: manage commands somewhere else
		Arrays.asList(
				//new PlayCommand(this),//these 3 require audio service
				//new SkipCommand(this),
				//new VolumeCommand(this),
				new MemeCommand(this, memeManager),
				new AddMemeCommand(this, memeManager)
		).stream().forEach(
				cmd -> {
					commands.put(cmd.getBase(), cmd);
				});

		clientProvider.getClient().getDispatcher().registerListener(this);
		busProvider.getBus().register(this);
	}

	@EventSubscriber
	public void onReady(ReadyEvent e) {
		LogUtil.logInfo("Connection ready.");
		onGuildCreateOrJoin(null);
		clientProvider.getClient().changeStatus(Status.game(playing));

		if (clientProvider.getClient().getGuilds().size() > maxServers) {
			busProvider.getBus().post(new ClientAbortEvent("Too many servers."));
		} else {
			busProvider.getBus().post(new ClientReadyEvent());
		}
	}

	@EventSubscriber
	public void onGuildCreateOrJoin(GuildCreateEvent e) {

		LogUtil.logInfo("Checking server connection limit...");
		try {
			if (clientProvider.getClient().getGuilds().size() > maxServers) {
				//leave unexpected guilds to keep server costs low
				e.getGuild().leaveGuild();
			} else {
				LogUtil.logInfo("Under connection limit, continuing...");
			}
		} catch (DiscordException | RateLimitException ex) {
			String m = "Connected to too many servers, but could not leave latest server. ABORT.";
			LogUtil.logError(m);
			busProvider.getBus().post(new ClientAbortEvent(m));
			dead = true;
		}
	}

	@EventSubscriber
	public void onMessage(MessageReceivedEvent event) {
		IMessage message = event.getMessage();
		IChannel channel = message.getChannel();
		IUser user = message.getAuthor();

		if (user.isBot()) {
			return;
		}

		LogUtil.logInfo(message.getGuild().getName() + ":" + channel.getName() + ":" + user.getName() + ": " + message.getContent());
		String[] split = message.getContent().split(" ");

		if (split.length >= 1 && split[0].startsWith(prefix)) {
			String command = split[0].replaceFirst(prefix, "");
			String[] args = split.length >= 2
					? Arrays.copyOfRange(split, 1, split.length)
					: new String[0];

			internalCommand(command, args, channel, user);
		}
	}

	//TODO: get rid of this completely with some guice
	public synchronized void internalCommand(String command, String[] args, IChannel channel, IUser user) {
		//TODO: decouple using event bus

		Command c = commands.get(command);
		if (c == null) {
			sendMessage(channel, "I'm sorry " + user.getName() + ". I'm afraid I can't do that.");
		} else {
			try {
				c.execute(args, channel, user);
			} catch (RateLimitException | DiscordException | MissingPermissionsException ex) {
				Logger.getLogger(Bot.class.getName()).log(Level.SEVERE, null, ex);
			}
		}
	}

	public YouTubeSearcher getYouTubeSearcher() {
		return youTubeSearcher;
	}

	@Override
	public IVoiceChannel joinUsersAudioChannel(IGuild guild, IUser user) throws CantJoinAudioChannelException {
		IVoiceChannel result = null;
		if (user.getConnectedVoiceChannels().size() < 1) {
			throw new CantJoinAudioChannelException("You aren't in a voice channel, " + user.getName() + ".");
		} else {
			IVoiceChannel voice = user.getConnectedVoiceChannels().get(0);
			if (!voice.getModifiedPermissions(clientProvider.getClient().getOurUser()).contains(Permissions.VOICE_CONNECT)) {
				throw new CantJoinAudioChannelException("Can't join " + voice.getName() + " without the voice permission!");
			} else if (voice.getUserLimit() != 0 && voice.getConnectedUsers().size() >= voice.getUserLimit()) {
				throw new CantJoinAudioChannelException("Can't join " + voice.getName() + ". It is already full.");
			} else {
				joinVoice(voice);
				result = voice;
			}
		}
		return result;
	}

	@Override
	public void sendMessage(IChannel channel, String message) {
		//LogUtil.logInfo("Sending message on " + channel.getName() + ": " + message);
		try {
			synchronized (channel) {
				channel.sendMessage(message);
			}
		} catch (MissingPermissionsException | RateLimitException | DiscordException ex) {
			LogUtil.logError(ex.getMessage());
		}
	}

	@Override
	public void joinVoice(IVoiceChannel channel) {
		try {
			channel.join();
		} catch (MissingPermissionsException ex) {
			Preconditions.checkArgument(false, "Can't join " + channel.getName() + ".");
		}
	}

}
