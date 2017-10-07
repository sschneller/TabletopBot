package commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.waiter.EventWaiter;

public class SessionCommand extends Command{

    private final EventWaiter waiter;

    public SessionCommand(EventWaiter waiter) {
        this.waiter = waiter;
        this.name = "session";
        this.help = "give session id";
    }

    protected void execute(CommandEvent event) {
        String s = event.getGuild().getId();
        event.reply("ID = " + s);
    }
}
