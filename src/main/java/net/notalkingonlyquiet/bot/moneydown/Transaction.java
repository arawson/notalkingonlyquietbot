package net.notalkingonlyquiet.bot.moneydown;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import javax.persistence.*;
import java.sql.Timestamp;

@Entity
public final class Transaction {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    private Long userID;
    private Long guildID;
    private Long amount;
    @Enumerated(EnumType.STRING) private TransactionType type;
    @Version private Long version;
    private Timestamp when;

    @ManyToOne
    private MoneyDown source;

    protected Transaction() {}

    public Transaction(IUser user, IGuild guild, long amount, TransactionType type) {
        this(user.getLongID(), guild.getLongID(), amount, type);
    }

    public Transaction(IUser user, IGuild guild, long amount, TransactionType type, MoneyDown source) {
        this(user.getLongID(), guild.getLongID(), amount, type);
        this.source = source;
    }

    public Transaction(long userID, long guildID, long amount, TransactionType type) {
        this.userID = userID;
        this.guildID = guildID;
        this.amount = amount;
        this.type = type;
        this.when = new Timestamp(System.currentTimeMillis());
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

    public Long getUserID() {
        return userID;
    }

    public Long getGuildID() {
        return guildID;
    }

    public Long getID() {
        return ID;
    }

    public Timestamp getWhen() {
        return when;
    }

    public MoneyDown getSource() { return source; }
}
