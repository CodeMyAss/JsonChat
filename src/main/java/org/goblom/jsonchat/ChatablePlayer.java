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
import java.util.UUID;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.goblom.jsonchat.events.AsyncJsonPlayerChatEvent;
import org.goblom.jsonchat.libs.fanciful.FancyMessage;

/**
 *
 * @author Goblom
 */
public class ChatablePlayer {
    
    private final UUID uuid;
    private List<String> tooltip;
    private String nameFormat;
    
    ChatablePlayer(Player player) {
        this.uuid = player.getUniqueId();
        this.tooltip = Lists.newArrayList();
        this.nameFormat = "<{name}> ";
    }
    
    public final UUID getUniqueId() {
        return this.uuid;
    }
    
    //Tooltip parser is handled in chat(String, Set<Player>)
    public List<String> getTooltip() {
        return this.tooltip;
    }
    
    public String getNameFormat() {
        if (getBukkit() == null) {
            return this.nameFormat;
        }
        
        String str = ChatColor.translateAlternateColorCodes('&', this.nameFormat);
        return JSONChat.modifyLine(new ModifierOutput(getBukkit()), str).getOutput().get(0);
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
            FancyMessage message = new FancyMessage(player.getNameFormat());
                         message.tooltip(output.getOutput());
                         message.then(event.getMessage());
                         
            JSONChatPlugin.send(message, event.getRecipients());
        }
    }
}
