package net.notalkingonlyquiet.bot.internal;

import net.notalkingonlyquiet.bot.util.LogUtil;
import net.notalkingonlyquiet.bot.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;
import sx.blah.discord.handle.impl.events.guild.GuildCreateEvent;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;
import sx.blah.discord.util.RequestBuffer;

/**
 * Prevents the bot from connecting to too many guilds.
 */
@Component
public class PreventOveruse implements BotInternalProc{
    @Autowired private IDiscordClient client;

    private int maxServers;

    @Autowired
    public PreventOveruse(Config config, IDiscordClient client, ComponentWaiter cw) {
        cw.notReady(this.getClass());
        maxServers = config.performance.servers;
        client.getDispatcher().registerListener(this);
        cw.ready(this.getClass());
    }

    @EventSubscriber
    public void onGuildJoin(GuildCreateEvent gce) {
        LogUtil.logInfo("Checking server connection limit...");
        try {
            if (client.getGuilds().size() > maxServers) {
                if (gce == null) {
                    //refuse to start if we have too many guilds at startup
                    throw new RuntimeException("Connected to too many servers on startup. ABORT.");
                } else {
                    //leave unexpected guilds to keep server costs low
                    gce.getGuild().leave();
                }
            } else {
                LogUtil.logInfo("Under connection limit, continuing...");
            }
        } catch (DiscordException | RateLimitException ex) {
            RequestBuffer.request(new RequestBuffer.IVoidRequest() {
                @Override
                public void doRequest() {
                    gce.getGuild().leave();
                }
            });
            throw new RuntimeException("Connected to too many servers, but could not leave latest server. ABORT.");
        }
    }

    //forward ready event to the main checker so that we can die on startup if we have too many guilds connected
    @EventSubscriber
    public void onReady(ReadyEvent e) {
        onGuildJoin(null);
    }
}
