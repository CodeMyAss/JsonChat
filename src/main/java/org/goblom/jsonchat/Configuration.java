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
import java.util.List;
import org.bukkit.ChatColor;

/**
 *
 * @author Goblom
 */
public class Configuration {
    private final JSONChatPlugin plugin;
    
    Configuration(JSONChatPlugin plugin) {
        this.plugin = plugin;
    }
    
    private boolean contains(String path) {
        return plugin.getConfig().contains(path);
    }
    
    private void set(String path, Object value) {
        plugin.getConfig().set(path, value);
        plugin.saveConfig();
    }
    
    public boolean getSetting(String name) {
        String path = "Settings." + name;
        
        if (!contains(path)) {
            set(path, true);
        }
        
        return plugin.getConfig().getBoolean(path);
    }
    
    public List<String> getTooltip(boolean format) {
        String path = "Format.Tooltip";
        if (!contains(path)) {
            set(path, Arrays.asList("UUID: {uuid}"));
        }
        
        List<String> list = plugin.getConfig().getStringList(path);
        
        if (!format) {
            return list;
        }
        
        List<String> formatted = Lists.newArrayList();
        
        for (String line : list) {
            formatted.add(ChatColor.translateAlternateColorCodes('&', line));
        }
        
        return formatted;
    }
    
    public String getNameFormat(boolean format) {
        String path = "Format.Name";
        if (!contains(path)) {
            set(path, "<{name}> ");
        }
        
        String name = plugin.getConfig().getString(path);
        
        if (!format) {
            return name;
        }
        
        return ChatColor.translateAlternateColorCodes('&', name);
    }
}
