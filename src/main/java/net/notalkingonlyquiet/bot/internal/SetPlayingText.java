package net.notalkingonlyquiet.bot.internal;

import net.notalkingonlyquiet.bot.config.Config;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.api.events.EventSubscriber;
import sx.blah.discord.handle.impl.events.ReadyEvent;

@Component
public class SetPlayingText implements BotInternalProc {
    private String playingText = "";

    @Autowired
    public SetPlayingText(Config config, IDiscordClient client, ComponentWaiter cw) {
        cw.notReady(this.getClass());
        playingText = config.login.playing;
        client.getDispatcher().registerListener(this);
        cw.ready(this.getClass());
    }

    @EventSubscriber
    public void onReady(ReadyEvent e) {
        e.getClient().changePlayingText(playingText);
    }
}
