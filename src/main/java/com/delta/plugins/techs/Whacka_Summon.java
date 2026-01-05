package com.delta.plugins.techs;

import com.delta.plugins.whacka.WhackaManager;
import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.cooldown.cooldownHelper;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;
import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;

import java.util.List;

public class Whacka_Summon {
    public static Technique summonWhackaTech = new Technique("whacka_summon", "Whacka Summoner", new TechniqueMeta(false, cooldownHelper.minutesToMiliseconds(10), List.of("Summons a friendly Whacka to fight for you.")), TargetSelectors.self(), (ctx, token) -> {
        Player player = ctx.caster();
        Entity e = WhackaManager.spawnWhackaFriendEntity(player.getLocation(), player);
        player.sendMessage("You have summoned a Whacka!");
        e.setCustomName(player.getName() + "'s Whacka");
    });
}
