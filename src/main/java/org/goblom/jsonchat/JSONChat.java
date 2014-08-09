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

import com.google.common.collect.Maps;
import java.util.Collection;
import java.util.Collections;
import java.util.List;
import java.util.Map;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;
import org.goblom.jsonchat.exceptions.InvalidModifierException;
import org.goblom.jsonchat.exceptions.ModifierNotFoundException;

/**
 *
 * @author Goblom
 */
public class JSONChat {
    private static final Map<String, Modifier> MODIFIERS = Maps.newHashMap();
    private static final JSONChatPlugin PLUGIN = JavaPlugin.getPlugin(JSONChatPlugin.class);
    
    public static Modifier getModifier(String lookFor) throws ModifierNotFoundException {
        if (MODIFIERS.containsKey(lookFor)) {
            return MODIFIERS.get(lookFor);
        }
        
        throw new ModifierNotFoundException(lookFor);
    }
    
    public static void registerModifier(Modifier mod) throws InvalidModifierException {
        if (mod.getLookingFor().isEmpty()) {
            throw new InvalidModifierException("Invalid search pattern for Modifier " + mod.getClass().getSimpleName());
        }
        
        if (mod.getDescription().isEmpty()) {
            throw new InvalidModifierException("Description for '" + mod.getLookingFor() + "' is empty");
        }
        
        if (!MODIFIERS.containsKey(mod.getLookingFor())) {
            String key = "{" + mod.getLookingFor() + "}";
            MODIFIERS.put(key, mod);
            PLUGIN.getLogger().info("Registered Modifier from plugin '" + mod.getProvidingPlugin() +"' with key: " + key);
        } else {
            throw new InvalidModifierException("Modifier '" + mod.getLookingFor() + "' is already registered");
        }
    }
    
    public static Collection<Modifier> getRegisteredModifiers() {
        return Collections.unmodifiableCollection(MODIFIERS.values());
    }
    
    protected static ModifierOutput modify(Player player, List<String> tooltip) {
        ModifierOutput output = new ModifierOutput(player);
                
        for (String line : tooltip) {
            modifyLine(output, line);
        }
        
        return output;
    }

    protected static ModifierOutput modifyLine(ModifierOutput output, String str) {
        for (String key : MODIFIERS.keySet()) {
            if (str.contains(key)) {
                Modifier mod = MODIFIERS.get(key);
                output.tooltip.add(str.replace(key, mod.onModify(output.getPlayer())));
                output.usedModifiers.add(mod);
            } else {
                output.tooltip.add(str);
            }
        }
        
        return output;
    }
}
