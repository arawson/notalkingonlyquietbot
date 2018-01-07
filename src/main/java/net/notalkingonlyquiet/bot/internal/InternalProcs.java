package net.notalkingonlyquiet.bot.internal;

import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.context.annotation.Scope;
import org.springframework.stereotype.Component;

import java.util.ArrayList;
import java.util.List;

@Component
@Scope(value = "singleton")
public class InternalProcs {
    private final List<BotInternalProc> internalProcs = new ArrayList<>();

    @Autowired
    public void setInternalProcs(List<BotInternalProc> procs) {
        internalProcs.clear();
        for (BotInternalProc p : procs) {
            internalProcs.add(p);
        }
    }

    public List<BotInternalProc> getInternalProcs() {
        return internalProcs;
    }
}
