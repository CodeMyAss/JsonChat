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
import java.net.URL;
import java.util.List;
import java.util.Set;
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.plugin.java.JavaPlugin;
import org.goblom.jsonchat.events.AsyncFancyMessageSendEvent;
import org.goblom.jsonchat.events.AsyncJsonPlayerChatEvent;
import org.goblom.jsonchat.libs.fanciful.FancyMessage;

/**
 *
 * @author Goblom
 */
public class ChatablePlayer {
    private static final JSONChatPlugin PLUGIN = JavaPlugin.getPlugin(JSONChatPlugin.class);
    
    private final UUID uuid;
    private List<String> tooltip;
    private String nameFormat;
    
    ChatablePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.tooltip = Lists.newArrayList();
    }
    
    public final UUID getUniqueId() {
        return this.uuid;
    }
    
    public List<String> getTooltip() {
        return this.tooltip;
    }
    
    public String getNameFormat() {
        if (getBukkit() == null) {
            return this.nameFormat;
        }
        
        return JSONChat.modifyLine(new ModifierOutput(getBukkit()), this.nameFormat).getOutput().get(0);
    }
    
    public void setCustomTooltip(List<String> tooltip) {
        this.tooltip = tooltip;
    }
    
    public void setCustomNameFormat(String nameFormat) {
        this.nameFormat = nameFormat;
    }
    
    public Player getBukkit() {
        return Bukkit.getPlayer(uuid);
    }
    
    protected void handle(AsyncPlayerChatEvent event) {
        event.setCancelled(true);
        
        chat(event.getMessage(), event.getRecipients());
    }
    
    public void chat(String message, Set<Player> recipients) {
        Player player = getBukkit();
        if (player == null) return;
        
        ModifierOutput output = JSONChat.modify(player, getTooltip());
        AsyncJsonPlayerChatEvent ajpce = new AsyncJsonPlayerChatEvent(player, message, recipients);
        
        ChatablePlayer.run(this, ajpce, output);
    }
    
    protected static void run(ChatablePlayer player, AsyncJsonPlayerChatEvent event, ModifierOutput output) {
        Bukkit.getPluginManager().callEvent(event);
        
        if (!event.isCancelled()) {
            FancyMessage message = new FancyMessage(ChatColor.translateAlternateColorCodes('&', player.getNameFormat()));
                         message.tooltip(colorList(output.getOutput()));
            
            //parse the meage
            for (String word : event.getMessage().split(" ")) {
                String clean = ChatColor.stripColor(word);
                try {
                    new URL(clean);
                    if (PLUGIN.getConfiguration().getSetting("Format-URL")) {
                        message.then("link").style(ChatColor.UNDERLINE);
                        message.tooltip(word);
                        message.link(clean);
                    } else {
                        message.then(word).link(clean);
                    }
                } catch (Exception e) {
                    message.then(word);
                }
                
                message.color(getLastColor(word));
                message.then(" ");
            }
            
            AsyncFancyMessageSendEvent afmse = new AsyncFancyMessageSendEvent(player.getBukkit(), message, event.getRecipients());
            Bukkit.getPluginManager().callEvent(afmse);
            JSONChatPlugin.send(message, afmse.getRecipients());
        }
    }
    
    protected static List<String> colorList(List<String> list) {
        List<String> newList = Lists.newArrayList();
        
        for (String line : list) {
            newList.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        
        return newList;
    }
    
    /**
     * Modified from
     * @see ChatColor#getLastColors(java.lang.String) 
     */
    protected static ChatColor getLastColor(String input) {
        int length = input.length();

        // Search backwards from the end as it is faster
        for (int index = length - 1; index > -1; index--) {
            char section = input.charAt(index);
            if (section == ChatColor.COLOR_CHAR && index < length - 1) {
                char c = input.charAt(index + 1);
                ChatColor color = ChatColor.getByChar(c);

                if (color != null) {

                    // Once we find a color or reset we can stop searching
                    if (color.isColor()) {
                        return color;
                    }
                }
            }
        }

        return ChatColor.WHITE;
    }
}
