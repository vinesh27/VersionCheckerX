package io.github.vinesh27.versioncheckerx;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.plugin.Plugin;
import org.bukkit.plugin.java.JavaPlugin;
import org.json.simple.JSONObject;
import org.json.simple.parser.JSONParser;
import org.json.simple.parser.ParseException;
import java.io.IOException;
import java.io.InputStream;
import java.io.InputStreamReader;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;
import java.util.Objects;

public final class VersionCheckerX extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        this.getCommand("versionChecker").setExecutor(this);
        Bukkit.getServer().getConsoleSender().sendMessage(
            ChatColor.GOLD + "" + ChatColor.BOLD + "[VersionCheckerX]" + ChatColor.RESET + "" + ChatColor.GREEN + " has started :)");
    }
    
    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage(
            ChatColor.GOLD + "" + ChatColor.BOLD + "[VersionCheckerX]" + ChatColor.RESET + "" + ChatColor.RED + " has stopped :(");
    }

    
    /**
     * All the commands are here
     */
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.isOp()) return false;
        //Checking if the command is related to the plugin
        if(cmd.getName().equalsIgnoreCase("versionChecker")) {
            if(args.length == 0) {
                sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[VersionCheckerX]" + ChatColor.RESET + "" + ChatColor.RED + " Please specify an option");
            }
            //For reloading the config.yml
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Plugin Reloaded!");
                getServer().getConsoleSender().sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + " [VersionCheckerX]" + ChatColor.RESET + "" + ChatColor.GREEN + ChatColor.GREEN + "Config Reloaded!");
                return true;
            }
            //For checking the version of the plugin from config.yml
            else if (args[0].equalsIgnoreCase("check")) {
                sender.sendMessage("Starting Search");
                //Just validating the IDs
                
                if (getConfig().getList("versionID.versionList") == null || getConfig().getList("versionID.versionList").size() == 0) {
                    sender.sendMessage("No version list found");
                    return false;
                }
                if (!(getConfig().getList("versionID.versionList") instanceof ArrayList)) {
                    sender.sendMessage("VersionList is not a list");
                    return false;
                }
                ArrayList<Integer> resources = (ArrayList<Integer>) getConfig().getIntegerList("versionID.versionList");
                for (Integer s : resources) {
                    //Getting info about the plugin using spigot API
                    JSONObject info = getInfo(String.valueOf(s));
                    if (info == null) {
                        sender.sendMessage("Failed to get info for " + s);
                        continue;
                    }
                    if(info.containsKey("code")){
                        sender.sendMessage("Error: " + info);
                        return false;
                    }
                    
                    String latestVersion = info.get("current_version").toString();
                    if(info.get("title") == null){
                        sender.sendMessage("Error: " + info);
                        return false;
                    }
                    String currentVersion = getInstalledVersion(info.get("title").toString());
                    if (currentVersion == null) {
                        sender.sendMessage("No version found for " + info.get("title").toString());
                        continue;
                    }
                    if (!Objects.equals(latestVersion, currentVersion)) {
                        sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                                ChatColor.GREEN + latestVersion + " | " +
                                ChatColor.RED + currentVersion);
                        sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " Link: " + getPluginLink(String.valueOf(s)));
                    } else {
                        sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                                ChatColor.GREEN + latestVersion + " | " +
                                ChatColor.GREEN + currentVersion);
                    }
                }
                return true;
            }
            //For checking the version of an individual plugin from args
            else if (args[0].equalsIgnoreCase("indi")){
                if(args.length < 2) {
                    sender.sendMessage(ChatColor.GOLD + "" + ChatColor.BOLD + "[VersionCheckerX]" + ChatColor.RESET + "" + ChatColor.RED + " Please specify a plugin");
                    return false;
                }
                String s = args[1];
                JSONObject info = getInfo(s);
                if (info == null) {
                    sender.sendMessage("Failed to get info for " + s);
                    return false;
                }
                //Logging the error, if it exists
                if(info.containsKey("code")){
                    sender.sendMessage("Error: " + info);
                    return false;
                }
                String latestVersion = info.get("current_version").toString();
                String currentVersion = getInstalledVersion(info.get("title").toString());
                if (currentVersion == null) {
                    sender.sendMessage("No version found for " + info.get("title").toString());
                    return false;
                }
                if (!Objects.equals(latestVersion, currentVersion)) {
                    sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                            ChatColor.GREEN + latestVersion + " | " +
                            ChatColor.RED + currentVersion);
                    sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " Link: " + getPluginLink(s));
                } else {
                    sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                            ChatColor.GREEN + latestVersion + " | " +
                            ChatColor.GREEN + currentVersion);
                }
                return true;
            }
            //Prints a little info about the developer (me :P)
            else if (args[0].equalsIgnoreCase("aboutMe")){
                String about =
                    """
                    *bHey there! Thanks for using the plugin! I'm a developer of this plugin.
                    *bMy name is Vinesh, A Java Programmer from India. I like to contribute to Open Source Projects.
                    *bI also upload stuff on my twitter & instagram about programming (twitter.com/vineshCodes || instagram.com/vineshCodes).
                    *bIf you would like to support this plugin, you can drop me a follow on my socials & github
                    *bI also like to stream on twitch (twitch.tv/vineshcodes)
                    *bIf you have any problems with this plugin, you can join my discord server and open a ticket (https://discord.gg/FmrEZSwXE4)
                    *bIf you want to contribute to this plugin, Check out the github repo (https://github.com/vinesh27/VersionCheckerX)
                    *bThank you for your time
                    """;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('*', about));
            }
            //Gives you a short guide on how to use the plugin
            else if (args[0].equalsIgnoreCase("help")) {
                String help =
                        """
                        *bVersionCheckerX Commands:
                        *b/versionChecker indi <ID> : Check the version of the plugin with the ID
                        *b/versionChecker check     : Check all the plugins from config.yml
                        *b/versionChecker reload    : Reload the config.yml
                        *b/versionChecker aboutMe   : Get to know about the Developer of this plugin
                        *b/versionChecker help      : Pulls up this menu
                        """;
                sender.sendMessage(ChatColor.translateAlternateColorCodes('*', help));
            }
        }
        return super.onCommand(sender, cmd, label, args);
    }
    
    /**
     * Gets the plugin info (name and version is used) from the ID
     * @param resourceId ID of the plugin
     * @return JSONObject
     */
    public JSONObject getInfo(String resourceId) {
        try {
            //Getting the JSON from the API
            String sURL = "https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=" + resourceId;
            URL url = new URL(sURL);
            URLConnection request = url.openConnection();
            request.connect();
            JSONParser jp = new JSONParser();
            return (JSONObject) jp.parse(new InputStreamReader((InputStream) request.getContent()));
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    
    /**
     * Gets the latest version of the plugin from the ID
     * @param name - Name of the plugin
     * @return String (version)
     */
    public String getInstalledVersion(String name) {
        //Gets the plugin from the 'plugins' folder, will return null if it is not installed
        Plugin plugin =  Bukkit.getPluginManager().getPlugin(name);
        if(plugin == null) return null;
        return plugin.getDescription().getVersion();
    }
    
    /**
     * Gets the plugin link from the ID
     * @param resourceId ID of the plugin
     * @return String (link)
     */
    public String getPluginLink(String resourceId) {
        try {
            //Getting the JSON from the API
            String sURL = "https://api.spiget.org/v2/resources/" + resourceId;
            URL url = new URL(sURL);
            URLConnection request = url.openConnection();
            request.connect();
            JSONParser jp = new JSONParser();
            JSONObject root = (JSONObject) jp.parse(new InputStreamReader((InputStream) request.getContent()));
            return ((JSONObject) root.get("file")).get("externalUrl").toString();
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
}