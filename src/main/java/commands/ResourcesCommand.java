package commands;

import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;

import java.util.ArrayList;

public class ResourcesCommand extends Command {
    ArrayList<String> resources;

    public ResourcesCommand(){
        resources = new ArrayList<>();
        this.name = "Resources";
        this.aliases = new String[]{"resources","Resource", "resource"};
        this.help = "Add a resource";
        this.arguments = "<item> <item> ...";
        this.guildOnly = false;
    }

    @Override
    protected void execute(CommandEvent event){

        if(event.getArgs().isEmpty()){
            event.reply("Please enter in a command");
        }

        else{
            String[] items = event.getArgs().split("\\s+");

            if(items.length == 1){
                if(items[0].equalsIgnoreCase("get")) {
                    if (resources.size() == 0) {
                        event.reply("No resources");
                    } else {
                        String resoureString = "";
                        for (String resource : resources) {
                            resoureString = resoureString + resource + "\n";
                        }
                        event.reply(resoureString);

                    }
                }
                else if(items[0].equalsIgnoreCase("help")){
                    event.reply("Available commands:\n\n"+
                            "add |resource|- adds the resource\n" +
                            "get - get all resources\n" +
                            "remove |resource| - removes the resource");
                }
                else {
                    event.reply("missing information");
                }
            }

            if(items[0].equalsIgnoreCase("add")){
                if(resources.add(items[1])){
                    event.reply("added " + items[1]);
                }
            }

            if(items[0].equalsIgnoreCase("remove")){
                if(resources.remove(items[1])){
                    event.reply("removed " + items[1]);
                }
                else{
                    event.reply("Could not remove " + items[1]);
                }
            }



        }
    }
}
