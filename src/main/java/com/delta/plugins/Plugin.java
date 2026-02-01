package com.delta.plugins;

import com.delta.plugins.advs.AdvancementTabNamespaces;
import com.delta.plugins.advs.pure_hearts.*;
import com.delta.plugins.advs.whacka_tab.*;
import com.delta.plugins.commands.*;
import com.delta.plugins.darkworld.DarkWorldEvents;
import com.delta.plugins.darkworld.DarkWorldRegistry;
import com.delta.plugins.events.PitEvents;
import com.delta.plugins.events.specialEvents.DeltaGraveEvent;
import com.delta.plugins.events.specialEvents.QuestionmarkAbyss;
import com.delta.plugins.items.Items;
import com.delta.plugins.items.PitItems;
import com.delta.plugins.techs.*;
import com.delta.plugins.whacka.WhackaListener;
import com.delta.plugins.whacka.WhackaLocationCommand;
import com.delta.plugins.whacka.WhackaManager;
import com.fren_gor.ultimateAdvancementAPI.AdvancementTab;
import com.fren_gor.ultimateAdvancementAPI.UltimateAdvancementAPI;
import com.fren_gor.ultimateAdvancementAPI.advancement.RootAdvancement;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementDisplay;
import com.fren_gor.ultimateAdvancementAPI.advancement.display.AdvancementFrameType;
import com.fren_gor.ultimateAdvancementAPI.util.AdvancementKey;
import com.fren_gor.ultimateAdvancementAPI.util.CoordAdapter;
import de.slikey.effectlib.EffectManager;
import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.ArrayList;
import java.util.List;
import java.util.logging.Logger;

/*
 * deltaplugin java plugin
 */
public class Plugin extends JavaPlugin implements Listener
{

    private static EffectManager effectManager;
  private static final Logger LOGGER=Logger.getLogger("deltaplugin");
  private static final List<String> abyssIds = new ArrayList<>();
  boolean miawzVer = false;
  public void onEnable()
  {
      effectManager = new EffectManager(this);

      Items.Init();
     miawzVer = (getConfig().getBoolean("key.miawz"));
     if(!miawzVer){

         PitItems.Init();
         DarkWorldRegistry.InitItems();
         initCommands();
         // Initialize techniques
         initTechniques();
         // Register events
         initEvents();
     }
      // Initialize advancement tabs
      initializeTabs();

      getServer().getPluginManager().registerEvents(this, this);
    // Register Whacka system
    WhackaLocationCommand.command.register();
    WhackaManager.init();
    getServer().getPluginManager().registerEvents(new WhackaListener(), this);

    getServer().getPluginManager().registerEvents(new PitEvents(), this);
    PitEvents.registerTimer();
    LOGGER.info("deltaplugin enabled");
    if(miawzVer){
        LOGGER.warning("Miawz version detected! Some features are disabled.");
    }
  }

    public static EffectManager getEffectManager() {
        return effectManager;
    }

    public static UltimateAdvancementAPI api;
  public AdvancementTab pure_hearts;
    public AdvancementTab whacka_tab;

  public void initializeTabs() {
    api = UltimateAdvancementAPI.getInstance(this);
    if(!miawzVer){

        pure_hearts = api.createAdvancementTab(AdvancementTabNamespaces.pure_hearts_NAMESPACE);
        ItemStack icon = new ItemStack(Material.LEATHER);
        ItemMeta meta = icon.getItemMeta();
        meta.setItemModel(NamespacedKey.minecraft("purity_heart"));
        icon.setItemMeta(meta);
        AdvancementKey purehearts_baseKey = new AdvancementKey(pure_hearts.getNamespace(), "purehearts_base");
        CoordAdapter adapterpure_hearts = CoordAdapter.builder().add(purehearts_baseKey, 0f, 0f).add(Pureheart_red.KEY, 1f, 0f).add(Pureheart_purple.KEY, 1f, 1f).add(Pureheart_pink.KEY, 1f, 2f).add(Pureheart_yellow.KEY, 1f, 3f).add(Pureheart_grey.KEY, 1f, 4f).add(Pureheart_brown.KEY, 1f, -1f).add(Pureheart_blue.KEY, 1f, -2f).add(Pureheart_cyan.KEY, 1f, -3f).add(Heart_chaos.KEY, 2f, 2.5f).add(Heart_purity.KEY, 4f, 0f).build();
        RootAdvancement purehearts_base = new RootAdvancement(pure_hearts, purehearts_baseKey.getKey(), new AdvancementDisplay(icon, "Una misión entre dimensiones", AdvancementFrameType.TASK, true, true, adapterpure_hearts.getX(purehearts_baseKey), adapterpure_hearts.getY(purehearts_baseKey), "El viaje no ha hecho más que comenzar"),"textures/block/tinted_glass.png",1);
        Pureheart_red pureheart_red = new Pureheart_red(purehearts_base,adapterpure_hearts.getX(Pureheart_red.KEY), adapterpure_hearts.getY(Pureheart_red.KEY));
        Pureheart_purple pureheart_purple = new Pureheart_purple(purehearts_base,adapterpure_hearts.getX(Pureheart_purple.KEY), adapterpure_hearts.getY(Pureheart_purple.KEY));
        Pureheart_pink pureheart_pink = new Pureheart_pink(purehearts_base,adapterpure_hearts.getX(Pureheart_pink.KEY), adapterpure_hearts.getY(Pureheart_pink.KEY));
        Pureheart_yellow pureheart_yellow = new Pureheart_yellow(purehearts_base,adapterpure_hearts.getX(Pureheart_yellow.KEY), adapterpure_hearts.getY(Pureheart_yellow.KEY));
        Pureheart_grey pureheart_grey = new Pureheart_grey(purehearts_base,adapterpure_hearts.getX(Pureheart_grey.KEY), adapterpure_hearts.getY(Pureheart_grey.KEY));
        Pureheart_brown pureheart_brown = new Pureheart_brown(purehearts_base,adapterpure_hearts.getX(Pureheart_brown.KEY), adapterpure_hearts.getY(Pureheart_brown.KEY));
        Pureheart_blue pureheart_blue = new Pureheart_blue(purehearts_base,adapterpure_hearts.getX(Pureheart_blue.KEY), adapterpure_hearts.getY(Pureheart_blue.KEY));
        Pureheart_cyan pureheart_cyan = new Pureheart_cyan(purehearts_base,adapterpure_hearts.getX(Pureheart_cyan.KEY), adapterpure_hearts.getY(Pureheart_cyan.KEY));
        Heart_chaos heart_chaos = new Heart_chaos(purehearts_base,adapterpure_hearts.getX(Heart_chaos.KEY), adapterpure_hearts.getY(Heart_chaos.KEY));
        Heart_purity heart_purity = new Heart_purity(purehearts_base,adapterpure_hearts.getX(Heart_purity.KEY), adapterpure_hearts.getY(Heart_purity.KEY));
        pure_hearts.registerAdvancements(purehearts_base ,pureheart_red ,pureheart_purple ,pureheart_pink ,pureheart_yellow ,pureheart_grey ,pureheart_brown ,pureheart_blue ,pureheart_cyan ,heart_chaos ,heart_purity );
    }
      whacka_tab = api.createAdvancementTab(AdvancementTabNamespaces.whacka_tab_NAMESPACE);
      RootAdvancement whacka_start = new RootAdvancement(whacka_tab, "whacka_start", new AdvancementDisplay(Items.whacka_bump, "Guaka-quest", AdvancementFrameType.TASK, true, true, 0f, 0f , "Criaturas casi extintas, se cree que", "los Guakas traen consigo grandes", "fortunas"),"textures/block/diamond_block.png",1);
      Bump_1 bump_1 = new Bump_1(whacka_start);
      Bump_two bump_two = new Bump_two(bump_1);
      Bumps_16 bumps_16 = new Bumps_16(bump_two);
      Bumps_32 bumps_32 = new Bumps_32(bumps_16);
      Bumps_64 bumps_64 = new Bumps_64(bumps_32);
      Bump_amethyst bump_amethyst = null;
      Bump_aqua bump_aqua = null;
      Bump_ruby bump_ruby = null;
      Bump_sapphire bump_sapphire = null;
      Bump_bronze bump_bronze = null;
        Bump_silver bump_silver = null;
        Bump_gold bump_gold = null;
        Bump_emerald bump_emerald = null;
        Bump_onyx bump_onyx = null;
      if(!miawzVer){
          bump_emerald = new Bump_emerald(whacka_start);
          bump_aqua = new Bump_aqua(bump_emerald);
          bump_sapphire = new Bump_sapphire(bump_aqua);
          bump_ruby = new Bump_ruby(bump_sapphire);
          bump_amethyst = new Bump_amethyst(bump_ruby);
          bump_bronze = new Bump_bronze(bump_amethyst);
          bump_silver = new Bump_silver(bump_bronze);
          bump_gold = new Bump_gold(bump_silver);
          bump_onyx = new Bump_onyx(whacka_start);
      }
      if(miawzVer) whacka_tab.registerAdvancements(whacka_start ,bump_1 ,bump_two ,bumps_16, bumps_32 ,bumps_64);
      else whacka_tab.registerAdvancements(whacka_start ,bump_1 ,bump_two ,bumps_16, bumps_32 ,bumps_64, bump_emerald ,bump_aqua ,bump_sapphire ,bump_ruby ,bump_amethyst ,bump_bronze ,bump_silver ,bump_gold ,bump_onyx);
      LOGGER.info("Initialized advancement tabs");
  }
  public void onDisable()
  {
    effectManager.dispose();
      LOGGER.info("deltaplugin disabled");
  }

  @EventHandler
  void onPlayerJoin(PlayerJoinEvent ev){
    if(!miawzVer) pure_hearts.grantRootAdvancement(ev.getPlayer());
    whacka_tab.grantRootAdvancement(ev.getPlayer());
  }

  static void initTechniques(){

      assasin.registerTechniques();
      berserker.registerTechniques();
      assaultant.registerTechniques();
      roaring_soul.register();
      musician.register();
      end_boss.register();
      poet.register();
      chosen_one.register();
      Exsolig.register();
      puppet.register();
      manticore.register();
      smasher.register();
      arcane.register();
      griffon.register();
      masterOfHearts.register();
      leader.register();
      lovers.register();
      queen.register();
      offspring.register();
      devourer.register();
      Redeemed.register();
      clowns.register();
      chaosWielder.register();
      sword_college.register();
      lynk.register();
      Familiar_love.register();
      QuestionmarkAbyss.registerTech();
      Necrozma.registerTechs();
      MasterOfTrials.register();
      Whacka_abyss.register();
      OriginDepleter.register();
      OriginEngine.register();
      OriginAider.register();
  }

  static void initCommands(){

      givesword.command.register();
      setAbyss.command.register();
      // Registrar el nuevo comando que da todas las abyss ID
      setAbyss.commandAll.register();
      reload.command.register();
      givePureHeart.command.register();
      removeAbyss.command.register();
      givesword.container.register();
      givesword.withdraw.register();
      Copyplayer.register();
      SpawnPureHeart.register();
      SummonPurityHeart.register();
      SummonChaosHeart.register();
      makeDarkWorld.register(getPlugin(Plugin.class));
      pureHeartPillar.register();
      SummonIndestructibleItem.command.register();
      setAbyss.command2.register();
      setAbyss.commandAll.register();
      StartTower.command.register();
      StartTower.nextFloor.register();
      StartTower.resetfloor.register();
      StartTower.addMobSpot.register();
      StartTower.setTowerSpawn.register();
      StartTower.saveCheckpoint.register();
      StartTower.loadCheckpoint.register();
      StartTower.giveExtraKey.register();
      givePureHeart.registerMaps();
      givePureHeart.invert.register();
      SetPlayerY.setLowestY();
      SetGraveCommand.register();
      GiveRainbowBump.register();
  }

  void initEvents(){
      getServer().getPluginManager().registerEvents(new com.delta.plugins.events.events(), this);
      getServer().getPluginManager().registerEvents(new com.delta.plugins.events.BossEvents(), this);
      getServer().getPluginManager().registerEvents(new com.delta.plugins.darkworld.DarkWorldEvents(), this);
      getServer().getPluginManager().registerEvents(new com.delta.plugins.events.techEvents.DestinyBondEvents(), this);
      Bukkit.getScheduler().scheduleSyncDelayedTask(this, DarkWorldEvents::runDarkWorldParticles); // 20 ticks = 1-second delay
      Bukkit.getScheduler().scheduleSyncDelayedTask(this, () ->{
          getServer().getPluginManager().registerEvents(new DeltaGraveEvent(), this);
      }); // 20 ticks = 1-second delay
  }


  public static List<String> getAllAbyssIDs(){
    return abyssIds;
  }
  public static void registerAbyssID(String id){
    if(!abyssIds.contains(id)){
      abyssIds.add(id);
    }
  }

  public static List<String> getSpecialAbysses(){
    return List.of("familiar_love");
  }
  public static boolean getMiawzVer(){
    Plugin plugin = getPlugin(Plugin.class);
      return plugin.miawzVer;
  }
}
