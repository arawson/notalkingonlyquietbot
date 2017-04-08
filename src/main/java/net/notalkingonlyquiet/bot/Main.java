package net.notalkingonlyquiet.bot;

import com.moandjiezana.toml.Toml;
import java.io.File;
import java.io.IOException;
import net.notalkingonlyquiet.bot.config.Config;
import sx.blah.discord.api.ClientBuilder;
import sx.blah.discord.api.IDiscordClient;
import sx.blah.discord.util.DiscordException;
import sx.blah.discord.util.RateLimitException;

public class Main {

    /**
     * @param args the command line arguments
     * @throws sx.blah.discord.util.DiscordException
     * @throws sx.blah.discord.util.RateLimitException
     * @throws java.io.IOException
     */
    public static void main(String[] args) throws DiscordException, RateLimitException, IOException {
        Toml toml = new Toml().read(new File("./config.toml"));
        Config config = toml.to(Config.class);
        
        LogUtil.logInfo("Attempting to connect to Discord...");

        IDiscordClient client = new ClientBuilder().withToken(config.login.token).build();
        
        Bot bot = new Bot(client, config);

        client.login();

        LogUtil.logInfo("Login Successful...");

        System.in.read();

        bot.forceShutdown();
        
        client.logout();
    }
}
