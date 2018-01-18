package net.notalkingonlyquiet.bot.moneydown;

import sx.blah.discord.handle.obj.IUser;

import javax.persistence.Entity;
import javax.persistence.GeneratedValue;
import javax.persistence.GenerationType;
import javax.persistence.Id;

@Entity
public class Participant {
    @Id
    @GeneratedValue(strategy = GenerationType.IDENTITY)
    private Long id;
    private Long userID;

    private Boolean inAgreement;

    protected Participant() {}

    public Participant(IUser user, Boolean inAgreement) {
        this.userID = user.getLongID();
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
