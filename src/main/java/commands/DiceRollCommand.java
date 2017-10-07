package commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import com.jagrosh.jdautilities.waiter.EventWaiter;

import java.util.Random;

/**
 * Created by Mike on 10/7/2017.
 */
public class DiceRollCommand extends Command{

    private final EventWaiter waiter;

    public DiceRollCommand(EventWaiter waiter)
    {
        this.waiter = waiter;
        this.name = "diceroll";
        this.aliases = new String[]{"dice","roll"};
        this.help = "!diceroll <number> - Roll a randomly generated number out of <number>\n    Aliases - !dice <number>, !roll <number>";
    }

    protected void execute(CommandEvent event) {
        if(event.getArgs().isEmpty())
            event.reply("This is the dice command. Give me a number!");
        else {
            String[] items = event.getArgs().split("\\s+");
            if (items.length > 1)
                event.replyWarning("You gave too many arguments! Just give me a number.");
            else {
                try {
                    Random r = new Random();
                    int max = Integer.parseInt(items[0]);
                    int roll = r.nextInt(max) + 1;
                    event.reply("You rolled a " + roll + " out of a possible " + max + "!");
                    if (roll == 1) event.replyWarning("CRITICAL FAILURE!");
                    else if (roll == max) event.replySuccess("CRITICAL SUCCESS!");
                } catch (NumberFormatException e) {
                    event.replyWarning("Please give a NUMBER as an argument!");
                }
            }
        }
    }
}
