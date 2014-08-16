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
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.goblom.jsonchat.libs.fanciful.FancyMessage;
import org.goblom.jsonchat.libs.net.amoebaman.util.Reflection;

/**
 * @todo Add a command to show all registered modifiers and descriptions
 * @author Goblom
 */
public class JSONChatPlugin extends JavaPlugin implements Listener {
    //Work this into the plugin later
    protected static final Pattern CHAT_PATTERN = Pattern.compile("(?<=\\{)(.*?)(?=\\})");
    private Configuration config;
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        config = new Configuration(this);
        Bukkit.getPluginManager().registerEvents(this, this);
        
        PluginCommand cmd = getCommand("jsonchat");
                      cmd.setDescription("Lists all registered Modifiers and their description");
                      cmd.setExecutor(this);
                      cmd.setAliases(Arrays.asList("js"));
                      cmd.setUsage("/jsonchat");
                      cmd.setPermission("jsonchat.list");
                      cmd.setPermissionMessage(ChatColor.RED + "You do not have permission to list the JSONChat Modifiers.");
                      
        try {
            JSONChat.registerModifier(new ChatModifier(this, "name", "Show name of the player") {
                @Override
                public String onModify(Player player) {
                    return player.getName();
                }
            });

            JSONChat.registerModifier(new ChatModifier(this, "uuid", "Show uuid of the player") {
                @Override
                public String onModify(Player player) {
                    return player.getUniqueId().toString();
                }
            });
        } catch (Exception e) {
            e.printStackTrace();
        }
    }
    
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        Collection<ChatModifier> l = JSONChat.getRegisteredModifiers();
        ChatModifier[] mods = l.toArray(new ChatModifier[l.size()]);
                
        FancyMessage message = new FancyMessage("Chat Modifiers (" + mods.length +"): ").color(ChatColor.GREEN);
        
        for (int i = 0; i < mods.length; i++) {
            ChatModifier mod = mods[i];
            message.then("{" + mod.getLookingFor() + "}").color(ChatColor.AQUA);
            message.tooltip(ChatColor.DARK_PURPLE + "Plugin: " + ChatColor.GRAY + mod.getProvidingPlugin(),
                            ChatColor.DARK_PURPLE + "Description: " + ChatColor.GRAY + mod.getDescription());
            if (!isLast(mods, i)) {
                message.then(", ").color(ChatColor.WHITE);
            }
        }
        
        message.send(sender);
        
        return true;
    }
    
    @EventHandler
    public void onPlayerChat(AsyncPlayerChatEvent event) {
        JSONChat.getChatable(event.getPlayer()).handle(event);
    }
    
    @EventHandler
    public void onPlayerJoin(PlayerJoinEvent event) {
        JSONChat.getChatable(event.getPlayer());
    }
    
    protected static void send(FancyMessage message, Set<Player> recipients) {
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
    
    protected Configuration getConfiguration() {
        return config;
    }
    
    //******************************************
    //*** To prepare to move away from Fanciful, but not yet
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
    
    private boolean isLast(Object[] array, int index) {
        return (array.length - 1) == index;
    }
}
