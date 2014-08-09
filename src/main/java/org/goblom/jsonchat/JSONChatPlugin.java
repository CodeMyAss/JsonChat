/*
 * Copyright (C) 2014 Goblom
 *
 * This program is free software; you can redistribute it and/or
 * modify it under the terms of the GNU General Public License
 * as published by the Free Software Foundation; either version 2
 * of the License, or (at your option) any later version.
 *
 * This program is distributed in the hope that it will be useful,
 * but WITHOUT ANY WARRANTY; without even the implied warranty of
 * MERCHANTABILITY or FITNESS FOR A PARTICULAR PURPOSE.  See the
 * GNU General Public License for more details.
 *
 * You should have received a copy of the GNU General Public License
 * along with this program; if not, write to the Free Software
 * Foundation, Inc., 59 Temple Place - Suite 330, Boston, MA  02111-1307, USA.
 */

package org.goblom.jsonchat;

import com.google.common.collect.Lists;
import java.util.Arrays;
import java.util.Collection;
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.PluginCommand;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.goblom.jsonchat.events.AsyncJsonPlayerChatEvent;
import org.goblom.jsonchat.libs.fanciful.FancyMessage;
import org.goblom.jsonchat.libs.net.amoebaman.util.Reflection;
import org.json.simple.JSONObject;

/**
 * @todo Add a command to show all registered modifiers and descriptions
 * @author Goblom
 */
public class JSONChatPlugin extends JavaPlugin implements Listener {
    //Work this into the plugin later
    protected static final Pattern CHAT_PATTERN = Pattern.compile("(?<=\\{)(.*?)(?=\\})");
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        
        PluginCommand cmd = getCommand("jsonchat");
                      cmd.setDescription("Lists all registered Modifiers and their description");
                      cmd.setExecutor(this);
                      cmd.setAliases(Arrays.asList("js"));
                      cmd.setUsage("/jsonchat");
                      cmd.setPermission("jsonchat.list");
                      cmd.setPermissionMessage(ChatColor.RED + "You do not have permission to list the Modifiers.");
                      
        try {
            TooltipDefaults.load(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Collection<Modifier> list = JSONChat.getRegisteredModifiers();
        sender.sendMessage(ChatColor.GOLD + "=========================");
        sender.sendMessage(ChatColor.GREEN + "JSONChat Modifiers");
        sender.sendMessage(ChatColor.GOLD + "=========================");
        
        for (Modifier mod : list) {
            sender.sendMessage(ChatColor.AQUA + "- " + ChatColor.GRAY + "{" + mod.getLookingFor() + "} " + ChatColor.AQUA + "-- " + ChatColor.GRAY + mod.getDescription());
        }
        
        return true;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        
        ModifierOutput output = JSONChat.modify(event.getPlayer(), getToolTip());
        AsyncJsonPlayerChatEvent ajpce = new AsyncJsonPlayerChatEvent(event.getPlayer(), event.getMessage(), event.getRecipients());
        run(ajpce, output);
    }
    
    private List<String> getToolTip() {
        List<String> list = getConfig().getStringList("Tooltip");
        List<String> newList = Lists.newArrayList();
        
        for (String line : list) {
            newList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        
        return newList;
    }
    
    private String getNameFormat(Player player) {
        String str = getConfig().getString("Name-Format");
        String name = ChatColor.translateAlternateColorCodes('&', str);
        
        return JSONChat.modifyLine(new ModifierOutput(player), name).getOutput().get(0);
    }
    
    private void run(AsyncJsonPlayerChatEvent event, ModifierOutput output) {
        Bukkit.getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            FancyMessage message = new FancyMessage(getNameFormat(event.getPlayer()));
                         message.tooltip(output.getOutput());
                         message.then(event.getMessage());
                         
            send(message, event.getRecipients());
        }
    }
    
    private void send(FancyMessage message, Set<Player> recipients) {
        try {
            Object packet = message.createChatPacket(message.toJSONString());

            for (Player player : recipients) {
                Object handle = Reflection.getHandle(player);
                Object connection = Reflection.getField(handle.getClass(), "playerConnection").get(handle);
                Reflection.getMethod(connection.getClass(), "sendPacket", Reflection.getNMSClass("Packet")).invoke(connection, packet);
            }
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    //******************************************
    //*** To Move away from Fanciful, but not yet
    //******************************************
    private String listToLines(List<String> list) {
        StringBuilder sb = new StringBuilder();
        
        for (int i = 0; i < list.size(); i++) {
            String line = list.get(i);
            sb.append(line);
            
            if (!isLast(list, i)) {
                sb.append('\n');
            }
        }
        
        return sb.toString();
    }
    
    private boolean isLast(List list, int index) {
        return (list.size() - 1) == index;
    }
    
    //not finished
    @Deprecated
    private JSONObject toJsonMessage(Player player, List<String> tooltip, String message) {
        JSONObject obj = new JSONObject();
        
        obj.put("color", "white");
        obj.put("text", getNameFormat(player));
        obj.put("hoverEvent", hoverEvent("show_text", tooltip));
        
        return obj;
    }
    
    private JSONObject hoverEvent(String action, List<String> lines) {
        JSONObject obj = new JSONObject();
        obj.put("action", action);
        obj.put("value", listToLines(lines));
        return obj;
    }
}
