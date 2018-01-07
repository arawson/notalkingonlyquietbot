package net.notalkingonlyquiet.bot.moneydown;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.List;
import java.util.Map;

@Entity
public class MoneyDown {
    @Id
    private Long id;
    private String description;
    private Long createdByUserId;

    @OneToMany(cascade = CascadeType.ALL)
    private List<MoneyDownParticipant> participants;

    private Long entry;
    private Long pool;
    private Timestamp expires;
    private Boolean resolved;
    private Boolean success;
    private Long winningUserId;

    @Version private Long version;

    protected MoneyDown() {}
}
