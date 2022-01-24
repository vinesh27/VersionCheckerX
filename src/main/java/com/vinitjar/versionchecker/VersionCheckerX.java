package com.vinitjar.versionchecker;

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
import java.net.MalformedURLException;
import java.net.URL;
import java.net.URLConnection;
import java.util.ArrayList;

public final class VersionCheckerX extends JavaPlugin {
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getServer().getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&', "&8[&VersionCheckerX&8] ") + " has started :)");
    }
    @Override
    public void onDisable() {
        Bukkit.getServer().getConsoleSender().sendMessage(
                ChatColor.translateAlternateColorCodes('&', "&8[&VersionCheckerX&8] ") + " has stopped :(");
    }
    @Override
    public boolean onCommand(CommandSender sender, Command cmd, String label, String[] args) {
        if(!sender.isOp()) return false;
        if(cmd.getName().equalsIgnoreCase("versionChecker")) {
            if (!(args.length == 1)) {
                sender.sendMessage(ChatColor.RED + "Please specify the mode");
                return false;
            }
            if (args[0].equalsIgnoreCase("reload")) {
                reloadConfig();
                sender.sendMessage(ChatColor.GREEN + "Plugin Reloaded!");
                getServer().getConsoleSender().sendMessage(ChatColor.translateAlternateColorCodes('&', "&8[&Version Checker&8] ") + ChatColor.GREEN + "Config Reloaded!");
                return true;
            } else if (args[0].equalsIgnoreCase("check")) {
                sender.sendMessage("Starting Search");
                ArrayList<String> resources = (ArrayList<String>) getConfig().getList("versionID.versionList");
                for (String s : resources) {
                    JSONObject info = getInfo(s);
                    if(info.containsKey("code")){
                        sender.sendMessage("Error: " + info.toString());
                        return false;
                    }
                    String latestVersion = info.get("current_version").toString();
                    String currentVersion = getInstalledVersion(info.get("title").toString());
                    if (latestVersion != currentVersion) {
                        sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                                ChatColor.GREEN + latestVersion + " | " +
                                ChatColor.RED + currentVersion);
                        sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " Link: " + getPluginLink(s));
                    } else {
                        sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                                ChatColor.GREEN + latestVersion + " | " +
                                ChatColor.GREEN + currentVersion);
                    }
                }
            } else if (args[0].equalsIgnoreCase("indi")){
                String s = args[1];
                JSONObject info = getInfo(s);
                if(info.containsKey("code")){
                    sender.sendMessage("Error: " + info.toString());
                    return false;
                }
                String latestVersion = info.get("current_version").toString();
                String currentVersion = getInstalledVersion(info.get("title").toString());
                if (latestVersion != currentVersion) {
                    sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                            ChatColor.GREEN + latestVersion + " | " +
                            ChatColor.RED + currentVersion);
                    sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " Link: " + getPluginLink(s));
                } else {
                    sender.sendMessage(ChatColor.GOLD + info.get("title").toString() + " " +
                            ChatColor.GREEN + latestVersion + " | " +
                            ChatColor.GREEN + currentVersion);
                }
            }
        }
        return super.onCommand(sender, cmd, label, args);
    }
    public JSONObject getInfo(String resourceId) {
        try {
            String sURL = "https://api.spigotmc.org/simple/0.1/index.php?action=getResource&id=" + resourceId;
            URL url = new URL(sURL);
            URLConnection request = url.openConnection();
            request.connect();
            JSONParser jp = new JSONParser();
            JSONObject root = (JSONObject) jp.parse(new InputStreamReader((InputStream) request.getContent()));
            return root;
        } catch (IOException | ParseException e) {
            e.printStackTrace();
        }
        return null;
    }
    public String getInstalledVersion(String name) {
        return Bukkit.getPluginManager().getPlugin(name).getDescription().getVersion();
    }
    public String getPluginLink(String resourceId) {
        try {
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