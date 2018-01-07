package net.notalkingonlyquiet.bot.application;

import net.notalkingonlyquiet.bot.util.LogUtil;
import net.notalkingonlyquiet.bot.config.Config;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.stereotype.Component;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;

@Component
public class DiscordClientFactory implements FactoryBean<IDiscordClient> {
    @Autowired
    private Config config;

    private static IDiscordClient client;

    @Override
    public IDiscordClient getObject() throws Exception {
        if (client == null) {
            LogUtil.logInfo("Attempting to connect to Discord...");

            client = new ClientBuilder()
                    .withToken(config.login.token)
                    .withRecommendedShardCount()
                    .build();
        }

        return client;
    }

    @Override
    public Class<IDiscordClient> getObjectType() {
        return IDiscordClient.class;
    }

    @Override
    public boolean isSingleton() {
        return true;
    }
}
