package net.notalkingonlyquiet.bot.moneydown;

import javax.persistence.Entity;
import javax.persistence.Id;

@Entity
public class MoneyDownParticipant {
    @Id private Long id;
    private Long userID;

    private Boolean inAgreement;

    protected MoneyDownParticipant() {}

    public MoneyDownParticipant(Long userID, Boolean inAgreement) {
        this.userID = userID;
        this.inAgreement = inAgreement;
    }

    public Long getId() {
        return id;
    }

    public Long getUserID() {
        return userID;
    }

    public Boolean isInAgreement() {
        return inAgreement;
    }

    public void setInAgreement(Boolean agreement) {
        this.inAgreement = agreement;
    }
}
