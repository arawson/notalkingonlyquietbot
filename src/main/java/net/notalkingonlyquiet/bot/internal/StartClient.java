package net.notalkingonlyquiet.bot.internal;

import net.notalkingonlyquiet.bot.util.LogUtil;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.scheduling.annotation.Scheduled;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.IDiscordClient;

@Component
public class StartClient implements BotInternalProc {
    @Autowired
    private ComponentWaiter waiter;
    private boolean started = false;

    @Autowired
    @Scheduled(fixedRate = 2000, initialDelay = 500)
    public void load(IDiscordClient client) {
        if (!started && waiter.areAllReady()) {
            client.login();
            LogUtil.logInfo("Logging into Discord...");
        }
    }
}
