package net.notalkingonlyquiet.bot.moneydown;

import com.google.common.base.MoreObjects;
import sx.blah.discord.handle.obj.IGuild;
import sx.blah.discord.handle.obj.IUser;

import javax.persistence.Column;
import javax.persistence.Embeddable;
import javax.persistence.Entity;
import java.io.Serializable;

/*
Discord Notes:
[8:41 PM] thebinaryblob: lets check it out
[8:41 PM] thebinaryblob: GRAND DAD?!?!?!
[8:41 PM] thebinaryblob: ok, so moneydown command
[8:42 PM] thebinaryblob: i'm thinking of keeping it simple for now
[8:42 PM] thebinaryblob: there's a bank value for each person in the server that would start at 50
[8:42 PM] thebinaryblob: admin commands to set these if need be
[8:43 PM] thebinaryblob: then the commands might go something like this:
[8:43 PM] thebinaryblob: !moneydown 30 evan can't finish this whole thing of cheez-its
[8:43 PM] thebinaryblob: then bot says
[8:44 PM] thebinaryblob: any takers on bet #22 (evan can't finish this whole thing of cheez-its)?
[8:44 PM] thebinaryblob: then you'd be in with !moneydown take 22
[8:45 PM] thebinaryblob: bot: rhypht is in for 30 on #22 (evan can't finish this whole thing of cheez-its), he has 10 so he will be in the trash if he loses
[8:46 PM] thebinaryblob: bot: aurion will receive 60 if he succeeds
[8:46 PM] thebinaryblob: then there's some timeout, maybe a minute?
[8:46 PM] thebinaryblob: then we'd need admins to vote to confirm the result at the end
[8:46 PM] thebinaryblob: something like !moneydown conclude 22 success
[8:47 PM] thebinaryblob: then another timeout and payoffs would be dished out and people would be put in the trash based on the vote
[8:47 PM] thebinaryblob: maybe just any user in the server can vote on it
[8:47 PM] thebinaryblob: @Rhypht @Aurion how's that sound?
[10:12 PM] Aurion: Oh shit
[10:12 PM] Aurion: lemme read through this here
[10:13 PM] Rhypht: fuck me same hang on didn't see this
[10:14 PM] Rhypht: @thebinaryblob yeah
[10:14 PM] Aurion: i just like the thought of the bot asking what everyone things about my capacity to finish that whole thing of cheez-its
[10:15 PM] Rhypht: aAH
[10:15 PM] Rhypht: Do you think the bot could kinda like
[10:15 PM] Rhypht: dynamically generate a picture of the user in the trash or something
[10:15 PM] Rhypht: aAH
[10:15 PM] Aurion: aAHH
[10:15 PM] Aurion: or just have it put a picture of a trash can
[10:15 PM] Aurion: or like
[10:15 PM] Aurion: randomly grab a picture of trash cans from google
[10:15 PM] Rhypht: yeahhh yeah
[10:15 PM] Aurion: dumpsters
[10:16 PM] Aurion: recepticles
[10:16 PM] Aurion: bins
[10:16 PM] Aurion: disposals
[10:16 PM] Rhypht: so it's always a surprise what kind of trash you're in for
[10:16 PM] Aurion: eetc
[10:16 PM] Aurion: yeah a trashy surprise~
[10:16 PM] Rhypht: yeahhh
[10:17 PM] Rhypht: I want to be able to say "take the donts"
[10:17 PM] Aurion: but yeah it sounds good
[10:17 PM] Aurion: money down as a concept
[10:17 PM] Aurion: as a concept
[10:17 PM] Rhypht: as a CONCEPT
[10:18 PM] Rhypht: the only thing I'm wondering is how it would differentiate between the two types of moneydowns
[10:19 PM] Rhypht: because I don't know if what you described could handle moneydowns with more than two outcomes, like moneymatches and shit like that
[10:20 PM] Rhypht: if the only results to tell the bot were "success" or "fail"
[11:04 PM] thebinaryblob: like a moneydown ffa
[11:05 PM] thebinaryblob: then the choices would just be who won wouldnt it?
[11:05 PM] Rhypht: yeah
[11:06 PM] Rhypht: with one extra option possibly to cancel the moneydown if no one that was in the moneydown won
[11:06 PM] thebinaryblob: yeah then it just puts us all in the trash
[11:06 PM] Rhypht: yes
[11:07 PM] thebinaryblob: it might also be possible for it to put your avatar in a random trash container
[11:07 PM] Rhypht: MAH DUDE
[11:08 PM] thebinaryblob: yeah, the api can get a link to a user's avatar
 */

@Embeddable
public class UserRef implements Serializable {
    @Column(nullable = false)
    private Long userId;

    @Column(nullable =  false)
    private Long guildId;

    protected UserRef() {}

    public UserRef(IUser u, IGuild g) {
        this.userId = u.getLongID();
        this.guildId = g.getLongID();
    }

    public Long getUserId() {
        return userId;
    }

    public Long getGuildId() {
        return guildId;
    }

    @Override
    public String toString() {
        return MoreObjects.toStringHelper(this)
                .add("userId", userId)
                .add("guildId", guildId)
                .toString();
    }
}
