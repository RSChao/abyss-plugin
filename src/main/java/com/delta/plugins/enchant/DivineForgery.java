package com.delta.plugins.enchant;

import com.rschao.plugins.showdowncore.showdownCore.api.enchantment.CustomEnchantment;
import com.rschao.plugins.showdowncore.showdownCore.api.enchantment.definition.EasyEnchant;
import com.rschao.plugins.showdowncore.showdownCore.api.enchantment.util.ColorCodes;

public class DivineForgery extends EasyEnchant {
    public DivineForgery() {
        super("divine_forgery", "showdowncore", ColorCodes.YELLOW.getCode() + ColorCodes.BOLD.getCode() + "Divine Forgery");
        CustomEnchantment enchantment = getCustomEnchantment();
        enchantment.setMaxLevel(3);
        enchantment.setSupportedItem("#minecraft:enchantable/sharp_weapon");
        enchantment.addExcludedEnchantment("minecraft:sharpness");
        this.saveBukkitEnchantment(enchantment);
    }
}