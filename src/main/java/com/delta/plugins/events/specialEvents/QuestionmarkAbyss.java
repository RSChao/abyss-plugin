package com.delta.plugins.events.specialEvents;


import com.rschao.plugins.techniqueAPI.tech.Technique;
import com.rschao.plugins.techniqueAPI.tech.TechniqueMeta;
import com.rschao.plugins.techniqueAPI.tech.register.TechRegistry;
import com.rschao.plugins.techniqueAPI.tech.selectors.TargetSelectors;

import java.util.ArrayList;

public class QuestionmarkAbyss {
    static final String TECH_ID = "?????";

    public static void registerTech() {
        TechRegistry.registerTechnique(TECH_ID, tech);
    }

    static Technique tech = new Technique("?????", "?????", new TechniqueMeta(false, 0, new ArrayList<>()), TargetSelectors.self(), (ctx, token) ->{
        GravesManager.onMysteriousAbyssUse(ctx.caster());
    });
}
