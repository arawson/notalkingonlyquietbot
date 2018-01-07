package net.notalkingonlyquiet.bot.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;

import javax.annotation.PreDestroy;

@Component
public class StopClient implements BotInternalProc {
    @Autowired
    private IDiscordClient client;

    @PreDestroy
    public void shutdown() {
        client.logout();
    }
}
