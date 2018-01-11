package net.notalkingonlyquiet.bot.moneydown;

import net.notalkingonlyquiet.bot.util.CommandUtil;
import org.hibernate.annotations.CreationTimestamp;

import javax.persistence.*;
import java.sql.Timestamp;
import java.util.*;

@Entity
public class MoneyDown {
    @Id private Long id;
    private Long guildId;
    private Long createdByUserId;

    private Timestamp startTime;
    private Timestamp lastTakeableTime;
    private Timestamp expiryTime;

    @Enumerated(EnumType.STRING) private BetType type;
    private String description;

    @OneToMany(cascade = CascadeType.ALL) private List<MoneyDownParticipant> participants;

    private Long entryCost;

    private Boolean resolved;

    //only one of the two
    private Boolean success;
    private Long winningUserId;

    @Version private Long version;

    protected MoneyDown() {}

    public MoneyDown(Long guildId, Long createdByUserId, BetType type, String description,
                     Long entryCost) {
        this.guildId = guildId;
        this.createdByUserId = createdByUserId;
        //TODO: limited to system time zone
        this.startTime = new Timestamp(System.currentTimeMillis());
        this.lastTakeableTime = CommandUtil.addTime(this.startTime, Calendar.MINUTE, 10);
        this.expiryTime = CommandUtil.addTime(this.lastTakeableTime, Calendar.DATE, 1);
        this.type = type;
        this.description = description;
        this.entryCost = entryCost;

        this.participants = new ArrayList<>();
        this.resolved = false;
        this.success = null;
        this.winningUserId = null;
    }

    public Long getId() {
        return id;
    }

    public Long getGuildId() {
        return guildId;
    }

    public Long getCreatedByUserId() {
        return createdByUserId;
    }

    public Timestamp getStartTime() {
        return startTime;
    }

    public Timestamp getLastTakeableTime() {
        return lastTakeableTime;
    }

    public Timestamp getExpiryTime() {
        return expiryTime;
    }

    public BetType getType() {
        return type;
    }

    public String getDescription() {
        return description;
    }

    public List<MoneyDownParticipant> getParticipants() {
        return participants;
    }

    public void setParticipants(List<MoneyDownParticipant> participants) {
        this.participants = participants;
    }

    public Long getEntryCost() {
        return entryCost;
    }

    public Boolean getResolved() {
        return resolved;
    }

    public void setResolved(Boolean resolved) {
        this.resolved = resolved;
    }

    public Boolean getSuccess() {
        return success;
    }

    public void setSuccess(Boolean success) {
        this.success = success;
    }

    public Long getWinningUserId() {
        return winningUserId;
    }

    public void setWinningUserId(Long winningUserId) {
        this.winningUserId = winningUserId;
    }
}
