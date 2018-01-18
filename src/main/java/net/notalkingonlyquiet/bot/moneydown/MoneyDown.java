package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.CommandUtil;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@Entity
public class MoneyDown {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long guildID;
    private Long createdByUserID;

    private Timestamp startTime;
    private Timestamp lastTakeableTime;

    @Enumerated(EnumType.STRING) private BetType type;
    private String description;

    @OneToMany(cascade = CascadeType.ALL, fetch = FetchType.EAGER)
    private List<Participant> participants;

    private Long entryCost;

    private Boolean resolved;

    private Boolean cancelled;

    //only one of the two
    private Boolean success;
    private Long winningUserId;

    @Version private Long version;

    protected MoneyDown() {}

    public MoneyDown(IGuild guild, IUser user, BetType type, String description,
                     Long entryCost) {
        this.guildID = guild.getLongID();
        this.createdByUserID = user.getLongID();
        //TODO: limited to system time zone
        this.startTime = new Timestamp(System.currentTimeMillis());
        this.lastTakeableTime = CommandUtil.addTime(this.startTime, Calendar.MINUTE, 10);
        this.type = type;
        this.description = description;
        this.entryCost = entryCost;

        this.participants = new ArrayList<>();
        this.resolved = false;
        this.cancelled = false;
        this.success = null;
        this.winningUserId = null;
    }

    public Long getId() {
        return id;
    }

    public Long getGuildID() {
        return guildID;
    }

    public Long getCreatedByUserID() {
        return createdByUserID;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getLastTakeableTime() {
        return lastTakeableTime;
    }

    public BetType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public List<Participant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<Participant> participants) {
        this.participants = participants;
    }

    public Long getEntryCost() {
        return entryCost;
    }

    public boolean isResolved() {
        return resolved == null ? false : resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public boolean isSuccessful() {
        return success == null ? false : success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public boolean getSuccess() {
        return success == null ? false : success;
    }

    public Long getWinningUserId() {
        return winningUserId;
    }

    public void setWinningUserId(Long winningUserId) {
        this.winningUserId = winningUserId;
    }

    public void setCancelled(Boolean cancelled) { this.cancelled = cancelled; }

    public boolean isCancelled() { return cancelled == null ? false : cancelled; }

    public long getPot() {
        return getEntryCost() * getParticipants().size();
    }
}
