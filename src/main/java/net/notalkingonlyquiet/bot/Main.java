package net.notalkingonlyquiet.bot;

import net.notalkingonlyquiet.bot.internal.ComponentWaiter;
import net.notalkingonlyquiet.bot.application.MainConfiguration;
import net.notalkingonlyquiet.bot.config.Config;
import net.notalkingonlyquiet.bot.internal.InternalProcs;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.boot.SpringApplication;
import org.springframework.boot.autoconfigure.SpringBootApplication;
import org.springframework.context.annotation.AnnotationConfigApplicationContext;

@SpringBootApplication
public class Main {

    private static ComponentWaiter waiter;

    @Autowired
    private void setWaiter(ComponentWaiter cw) {
        waiter = cw;
    }

    private static Config config;

    @Autowired
    private void setConfig(Config c) {
        config = c;
    }

    private static InternalProcs internals;

    @Autowired
    private void setInternals(InternalProcs p) { internals = p; }

    public static void main(String[] args) {
        AnnotationConfigApplicationContext ctx = new AnnotationConfigApplicationContext();

        //TODO: add support for config file for things that need it
        ctx.register(MainConfiguration.class);
        ctx.registerShutdownHook();

        SpringApplication.run(Main.class);
    }
}
