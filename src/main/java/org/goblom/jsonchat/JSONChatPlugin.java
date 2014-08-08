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
import java.util.List;
import java.util.Set;
import java.util.regex.Pattern;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.goblom.jsonchat.events.AsyncJsonPlayerChatEvent;
import org.goblom.jsonchat.libs.fanciful.FancyMessage;
import org.goblom.jsonchat.libs.net.amoebaman.util.Reflection;

/**
 * @todo Add a command to show all registered modifiers and descriptions
 * @author Goblom
 */
public class JSONChatPlugin extends JavaPlugin implements Listener {
    //Work this into the plugin later
    protected static final Pattern CHAT_PATTERN = Pattern.compile("(?<=[)(.*?)(?=])");
    
    @Override
    public void onEnable() {
        saveDefaultConfig();
        Bukkit.getPluginManager().registerEvents(this, this);
        
        try {
            TooltipDefaults.load(this);
        } catch (Exception e) {
            e.printStackTrace();
        }
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
}
