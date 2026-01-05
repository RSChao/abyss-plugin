package com.delta.plugins.events.specialEvents;

import com.rschao.plugins.techapi.tech.Technique;
import com.rschao.plugins.techapi.tech.register.TechRegistry;

public class QuestionmarkAbyss {
    static final String TECH_ID = "?????";

    public static void registerTech() {
        TechRegistry.registerTechnique(TECH_ID, tech);
    }

    static Technique tech = new Technique("?????", "?????", false, 0, ((player, itemStack, objects) -> {
        GravesManager.onMysteriousAbyssUse(player);
    }));
}
