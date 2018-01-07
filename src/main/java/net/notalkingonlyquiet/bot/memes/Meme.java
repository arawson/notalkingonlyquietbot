package net.notalkingonlyquiet.bot.memes;

import com.google.common.base.MoreObjects;
import org.hibernate.annotations.CreationTimestamp;
import org.hibernate.annotations.Generated;
import sx.blah.discord.handle.obj.IGuild;

import javax.persistence.*;
import java.util.Date;

@Entity
public class Meme {
    @Id
    @GeneratedValue(strategy = GenerationType.AUTO)
    private Long ID;

    private Long guildId;

    @Enumerated(EnumType.STRING)
    private MemeType type;

    private String value;

    @Basic(optional = false)
    @Temporal(TemporalType.TIMESTAMP)
    @CreationTimestamp
    private Date dateAdded = new Date();

    protected Meme() {}

    public Meme(Long guildId, MemeType type, String value) {
        this.guildId = guildId;
        this.type = type;
        this.value = value;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("ID", ID)
                .add("guild", guildId)
                .add("type", type)
                .add("value", value)
                .toString();
    }

    public Long getGuildId() {
        return guildId;
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
