package commands;

import com.google.gson.Gson;
import com.jagrosh.jdautilities.commandclient.Command;
import com.jagrosh.jdautilities.commandclient.CommandEvent;
import net.dv8tion.jda.core.EmbedBuilder;
import net.dv8tion.jda.core.entities.MessageEmbed;

import java.io.BufferedReader;
import java.io.DataOutputStream;
import java.io.InputStreamReader;
import java.net.HttpURLConnection;
import java.net.URL;
import java.util.*;

/**
 * Created by Mike on 10/7/2017.
 */
public class CombatCommand extends Command {

    private final String USER_AGENT = "Mozilla/5.0";

    public CombatCommand() {
        this.name = "combat";
        this.help = "get match of entity from list by ID";
    }
    protected void execute(CommandEvent event) {
        String id = event.getGuild().getId();
        String[] args = event.getArgs().split("\\s+");
        if (event.getArgs().isEmpty()) {
            event.replyError("Syntax error: no subcommand given. Type !help for usage details.");
        } else {
            if (args[0].equals("add")) {
                if (args.length < 3) {
                    event.replyError("Syntax error: insufficient args. Type !help for usage details.");
                } else {
                    try {
                        String name = args[1];
                        int init = Integer.parseInt(args[2]);
                        Gson gson = new Gson();
                        Entity[] e = gson.fromJson(sendGet("http://localhost:8080/entity"), Entity[].class);
                        for (Entity entity : e) {
                            if (entity.getName().equals(name)) {
                                int ent_id = entity.getId();
                                CombatOrder[] co = gson.fromJson(sendGet("http://localhost:8080/combat"), CombatOrder[].class);
                                boolean exists = false;
                                for (CombatOrder order : co) {
                                    if (order.getEntity_id() == ent_id)
                                        exists = true;
                                }
                                if (!exists) {
//                                    System.out.println("discord_id="
//                                            + id + "&entity_id=" + name + "&initiative=" + init);
                                    sendPost("http://localhost:8080/combat", "discord_id="
                                            + id + "&entity_id=" + ent_id + "&initiative=" + init);
                                    event.reply("Character " + name + " with initiative score " + init + " found!");
                                } else {
                                    event.replyWarning("An entity with name " + name + " already has been recorded!");
                                }
                                break;
                            }
                        }
                    } catch (NumberFormatException e) {
                        event.replyError("Syntax error: insufficient args. Type !help for usage details.");
                    } catch (Exception e) {
                        e.printStackTrace();
                        event.replyError("Something borked. Contact your server admin plz.");
                    }
                }
            } else if (args[0].equals("remove")) {
                if (args.length < 2) {
                    event.replyError("Syntax error: insufficient args. Type !help for usage details.");
                } else {
                    try {
                        String name = args[1];
                        Gson gson = new Gson();
                        Entity[] e = gson.fromJson(sendGet("http://localhost:8080/entity"), Entity[].class);
                        int ent_id;
                        for (Entity ent : e) {
                            if (ent.getName().equals(name)) {
                                ent_id = ent.getId();
                                System.out.println(sendGet("http://localhost:8080/combat"));
                                CombatOrder[] co = gson.fromJson(sendGet("http://localhost:8080/combat"), CombatOrder[].class);
                                boolean exists = false;
                                int combat_id = -1;
                                for (CombatOrder order : co) {
                                    if (order.getEntity_id() == ent_id)
                                        combat_id = order.getId();
                                        exists = true;
                                }
                                if (exists && combat_id != -1) {
                                    sendDelete("http://localhost:8080/combat/" + combat_id + "/");
                                    event.reply("Character " + name + " deleted from combat list.");
                                }
                                else
                                {
                                    event.replyWarning("Character " + name + " not found.");
                                }
                                break;
                            }
                        }
                    } catch (Exception e) {
                        e.printStackTrace();
                        event.replyError("Something borked. Contact your server admin plz.");
                    }
                }
            } else if (args[0].equals("list")) {
                try {
                    Gson gson = new Gson();
                    ArrayList<CombatOrder> ordersList = new ArrayList<>();
                    Map<String,Integer> playerEntries = new HashMap<>();
                    CombatOrder[] co = gson.fromJson(sendGet("http://localhost:8080/combat"), CombatOrder[].class);
                    Entity[] e = gson.fromJson(sendGet("http://localhost:8080/entity"), Entity[].class);
                    for (CombatOrder order : co) {
                        if (id.equals(order.getDiscord_id())) {
                            ordersList.add(order);
                        }
                    }
                    for (CombatOrder listedOrder : ordersList) {
                        for (Entity ent : e) {
//                            System.out.println(ent.getId());
//                            System.out.println(listedOrder.getEntity_id());
                            if (ent.getId().equals(listedOrder.getEntity_id())) {
                                playerEntries.put(ent.getName(), listedOrder.getInitiative());
                            }
                        }
                    }
                    if (playerEntries.size() > 0) {
                        //sort map entries into a list and print
                        List<Map.Entry<String,Integer>> entries = new ArrayList<Map.Entry<String,Integer>>(playerEntries.entrySet());
                        Collections.sort(entries, new Comparator<Map.Entry<String,Integer>>() {
                            @Override
                            public int compare(Map.Entry<String,Integer> e1, Map.Entry<String,Integer> e2) {
                                return e2.getValue().compareTo(e1.getValue());
                            }
                        });
                        EmbedBuilder embed = new EmbedBuilder();
                        embed.setTitle("COMBAT LIST");

                        String names = "";
                        String inits = "";
                        for (int i = 0; i < entries.size(); i++) {
//                            embed.appendDescription(entries.get(i).getKey() + " - Initiative = " + entries.get(i).getValue() + "\n");
                            names = names + (entries.get(i).getKey() + "\n");
                            inits = inits + (entries.get(i).getValue().toString() + "\n");
                        }
                        embed.addField("NAME", names,true);
                        embed.addField("INITIATIVE",inits,true);
                        event.reply(embed.build());
                    }
                } catch (Exception e) {
                    e.printStackTrace();
                    event.replyError("Something borked. Contact your server admin plz.");
                }
            }
        }

    }

    private String sendGet(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("GET");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();
    }

    private String sendPut(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is GET
        con.setRequestMethod("PUT");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'GET' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        return response.toString();
    }

    private void sendPost(String url, String urlParameters) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        //add reuqest header
        con.setRequestMethod("POST");
        con.setRequestProperty("User-Agent", USER_AGENT);
        con.setRequestProperty("Accept-Language", "en-US,en;q=0.5");

        // Send post request
        con.setDoOutput(true);
        DataOutputStream wr = new DataOutputStream(con.getOutputStream());
        wr.writeBytes(urlParameters);
        wr.flush();
        wr.close();

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'POST' request to URL : " + url);
        System.out.println("Post parameters : " + urlParameters);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

        //print result
        System.out.println(response.toString());

    }

    private void sendDelete(String url) throws Exception {
        URL obj = new URL(url);
        HttpURLConnection con = (HttpURLConnection) obj.openConnection();

        // optional default is DELETE
        con.setRequestMethod("DELETE");

        //add request header
        con.setRequestProperty("User-Agent", USER_AGENT);

        int responseCode = con.getResponseCode();
        System.out.println("\nSending 'DELETE' request to URL : " + url);
        System.out.println("Response Code : " + responseCode);

        BufferedReader in = new BufferedReader(
                new InputStreamReader(con.getInputStream()));
        String inputLine;
        StringBuffer response = new StringBuffer();

        while ((inputLine = in.readLine()) != null) {
            response.append(inputLine);
        }
        in.close();

//        //print result
//        return response.toString();
    }
}
