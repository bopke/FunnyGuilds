package net.dzikoysk.funnyguilds.event.guild;

import net.dzikoysk.funnyguilds.basic.Guild;
import net.dzikoysk.funnyguilds.basic.User;
import net.dzikoysk.funnyguilds.event.FunnyEvent;

public abstract class GuildEvent extends FunnyEvent {

    private final Guild guild;

    public GuildEvent(EventCause eventCause, User doer, Guild guild) {
        super(eventCause, doer);

        this.guild = guild;
    }

    public Guild getGuild() {
        return this.guild;
    }
    
}
