package net.notalkingonlyquiet.bot.moneydown;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import javax.persistence.*;
import java.sql.Time;
import java.sql.Timestamp;
import java.time.LocalDateTime;

@Entity
public final class MoneyDownTransaction {
    @EmbeddedId private UserRef user;
    private Long amount;
    @Enumerated(EnumType.STRING) private TransactionType type;
    @Version private Long version;
    private Timestamp when;

    protected MoneyDownTransaction() {}

    public MoneyDownTransaction(IUser u, IGuild g, long amount, TransactionType type) {
        this.user = new UserRef(u, g);
        this.amount = amount;
        this.type = type;
        this.when = Timestamp.valueOf(LocalDateTime.now());
    }

    public Long getAmount() {
        return amount;
    }

    public Long getVersion() {
        return version;
    }

    public TransactionType getType() {
        return type;
    }

    public UserRef getUser() {
        return user;
    }

    public Timestamp getWhen() {
        return when;
    }
}
