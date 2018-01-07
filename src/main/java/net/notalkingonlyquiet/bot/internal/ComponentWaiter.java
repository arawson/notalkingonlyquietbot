package net.notalkingonlyquiet.bot.internal;

import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.concurrent.ConcurrentHashMap;

/**
 * Used by beans to register and track when they are done loading so the client can login.
 * This is only needed by beans that need to wait for the onReadyEvent.
 * This isn't a sound approach as the discord client will be loaded before anything else even has a chance to notify
 * that it needs the onReady, so the StartClient class will wait a couple seconds to ensure that.
 */
@Component
@Scope(value = "singleton")
public class ComponentWaiter implements BotInternalProc {
    //it is only expected one instance of anything will ever need this
    private final ConcurrentHashMap<Class, Boolean> readies =  new ConcurrentHashMap<>();

    public void notReady(Class c) {
        readies.put(c, Boolean.FALSE);
    }

    public void ready(Class c) {
        readies.put(c, Boolean.TRUE);
    }

    public boolean areAllReady() {
        return !readies.containsValue(Boolean.FALSE);
    }
}
