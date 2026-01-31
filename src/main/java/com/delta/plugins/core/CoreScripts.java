package com.delta.plugins.core;

import com.rschao.plugins.showdowncore.showdownCore.api.runnables.ShowdownScript;
import com.rschao.plugins.showdowncore.showdownCore.api.runnables.registry.ScriptRegistry;
import org.bukkit.entity.Player;
import org.bukkit.potion.PotionEffectType;

public class CoreScripts {

    public void register(){
        ScriptRegistry.registerScript("utils:buff", buff);
    }
    public static ShowdownScript<Void> buff = new ShowdownScript<Void>((args) ->{
        Player player = (Player) args[0];
        if(player == null) return null;
        int level = (int) args[1];
        if(level<1) level = 1;
        player.addPotionEffect(PotionEffectType.SPEED.createEffect(90*20, level));
        player.addPotionEffect(PotionEffectType.STRENGTH.createEffect(90*20, level));
        player.addPotionEffect(PotionEffectType.RESISTANCE.createEffect(90*20, level));
        player.addPotionEffect(PotionEffectType.FIRE_RESISTANCE.createEffect(90*20, level));
        return null;
    });
}
