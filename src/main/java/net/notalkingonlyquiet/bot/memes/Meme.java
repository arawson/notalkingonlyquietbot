package net.notalkingonlyquiet.bot.memes;

import com.google.common.base.MoreObjects;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Meme {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long ID;

    private Long guildID;

    @Enumerated(EnumType.STRING)
    private MemeType type;

    private String value;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date dateAdded = new Date();

    protected Meme() {}

    public Meme(Long guildID, MemeType type, String value) {
        this.guildID = guildID;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ID", ID)
                .add("guild", guildID)
                .add("type", type)
                .add("value", value)
                .toString();
    }

    public Long getGuildID() {
        return guildID;
    }

    public Long getID() {
        return ID;
    }

    public MemeType getType() {
        return type;
    }

    public String getValue() {
        return value;
    }
}
