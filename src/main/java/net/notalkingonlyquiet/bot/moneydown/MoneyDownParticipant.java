package net.notalkingonlyquiet.bot.moneydown;

import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import javax.persistence.EmbeddedId;
import javax.persistence.Entity;

@Entity
public class MoneyDownParticipant {
    @EmbeddedId
    private UserRef user;

    private Boolean agreement;

    protected MoneyDownParticipant() {}

    public MoneyDownParticipant(IUser u, IGuild g, Boolean agreement) {
        this.user = new UserRef(u, g);
        this.agreement = agreement;
    }

    public UserRef getUser() {
        return user;
    }

    public Boolean getAgreement() {
        return agreement;
    }
}
