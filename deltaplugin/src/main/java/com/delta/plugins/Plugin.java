package com.delta.plugins;

import java.util.logging.Logger;
import org.bukkit.plugin.java.JavaPlugin;

import com.delta.plugins.commands.givesword;
import com.delta.plugins.commands.reload;
import com.delta.plugins.commands.setAbyss;
import com.delta.plugins.items.Items;
import com.delta.plugins.techs.assasin;
import com.delta.plugins.techs.assaultant;
import com.delta.plugins.techs.berserker;

/*
 * deltaplugin java plugin
 */
public class Plugin extends JavaPlugin
{
  private static final Logger LOGGER=Logger.getLogger("deltaplugin");

  public void onEnable()
  {
    Items.Init();
    LOGGER.info("deltaplugin enabled");
    givesword.command.register();
    setAbyss.command.register();
    reload.command.register();
    // Initialize techniques
    assasin.registerTechniques();
    berserker.registerTechniques();
    assaultant.registerTechniques();
    // Register events
    getServer().getPluginManager().registerEvents(new com.delta.plugins.events.events(), this);
  }

  public void onDisable()
  {
    LOGGER.info("deltaplugin disabled");
  }
}
