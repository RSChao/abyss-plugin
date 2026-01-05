package com.delta.plugins.commands;

import com.delta.plugins.items.Items;
import dev.jorel.commandapi.CommandAPICommand;
import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;

import java.util.HashMap;
import java.util.Map;

public class givePureHeart {

    static Map<ItemStack, ItemStack> hearts = new HashMap<>();
    static Map<ItemStack, ItemStack> Invertedhearts = new HashMap<>();
    public static void registerMaps(){
        hearts.put(Items.pure_heart_red, Items.pureheart_red);
        hearts.put(Items.pure_heart_brown, Items.pureheart_orange);
        hearts.put(Items.pure_heart_blue, Items.pureheart_yellow);
        hearts.put(Items.pure_heart_cyan, Items.pureheart_green);
        hearts.put(Items.pure_heart_purple, Items.pureheart_blue);
        hearts.put(Items.pure_heart_pink, Items.pureheart_indigo);
        hearts.put(Items.pure_heart_yellow, Items.pureheart_purple);
        hearts.put(Items.pure_heart_grey, Items.pureheart_white);


        Invertedhearts.put(Items.pureheart_red, Items.pure_heart_red);
        Invertedhearts.put(Items.pureheart_orange, Items.pure_heart_brown);
        Invertedhearts.put(Items.pureheart_yellow, Items.pure_heart_blue);
        Invertedhearts.put(Items.pureheart_green, Items.pure_heart_cyan);
        Invertedhearts.put(Items.pureheart_blue, Items.pure_heart_purple);
        Invertedhearts.put(Items.pureheart_indigo, Items.pure_heart_pink);
        Invertedhearts.put(Items.pureheart_purple, Items.pure_heart_yellow);
        Invertedhearts.put(Items.pureheart_white, Items.pure_heart_grey);
    }
    public static CommandAPICommand command = new CommandAPICommand("pureheart")
            .withPermission("delta.purehearts")
            .withSubcommands(original(), red(), brown(), blue(), cyan(), purple(), pink(), yellow(), grey(), nightmare())
            .executesPlayer((player, args) -> {

            });
    static CommandAPICommand nightmare(){
         return new CommandAPICommand("nightmareheart")
                 .executesPlayer((player, args) -> {
                     player.getInventory().addItem(Items.nightmare);
                 });
     }
    static CommandAPICommand red(){
        return new CommandAPICommand("red")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_red);
                });
    }
    static CommandAPICommand brown(){
        return new CommandAPICommand("brown")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_brown);
                });
    }
    static CommandAPICommand blue(){
        return new CommandAPICommand("blue")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_blue);
                });
    }
    static CommandAPICommand cyan(){
        return new CommandAPICommand("cyan")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_cyan);
                });
    }
    static CommandAPICommand purple(){
        return new CommandAPICommand("purple")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_purple);
                });
    }
    static CommandAPICommand pink(){
        return new CommandAPICommand("pink")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_pink);
                });
    }
    static CommandAPICommand yellow(){
        return new CommandAPICommand("yellow")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_yellow);
                });
    }
    static CommandAPICommand grey(){
        return new CommandAPICommand("grey")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pure_heart_grey);
                });
    }





    static CommandAPICommand original(){
        return new CommandAPICommand("original")
                .withSubcommands(Red(), Orange(), Yellow(), Green(), Blue(), Indigo(), Purple(), White())
                .executesPlayer((player, args) -> {
                });
    }

    static CommandAPICommand Red(){
        return new CommandAPICommand("red")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_red);
                });
    }

    static CommandAPICommand Orange(){
        return new CommandAPICommand("orange")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_orange);
                });
    }

    static CommandAPICommand Yellow(){
        return new CommandAPICommand("yellow")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_yellow);
                });
    }

    static CommandAPICommand Green(){
        return new CommandAPICommand("green")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_green);
                });
    }

    static CommandAPICommand Blue(){
        return new CommandAPICommand("blue")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_blue);
                });
    }

    static CommandAPICommand Indigo(){
        return new CommandAPICommand("indigo")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_indigo);
                });
    }

    static CommandAPICommand Purple(){
        return new CommandAPICommand("purple")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_purple);
                });
    }

    static CommandAPICommand White(){
        return new CommandAPICommand("white")
                .executesPlayer((player, args) -> {
                    player.getInventory().addItem(Items.pureheart_white);
                });
    }

    public static CommandAPICommand invert = new CommandAPICommand("invertheart")
                .withPermission("delta.items")
                .withSubcommands(FromOriginal(), FromInverted())
                .executesPlayer((player, args) -> {

                });

    static CommandAPICommand FromOriginal(){
        return new CommandAPICommand("start")
                .withPermission("delta.items")
                .executesPlayer((player, args) -> {
                    int convertedItems = 0;
                    for(ItemStack item : player.getInventory().getContents()){
                        if(item == null) continue;
                        if(item.getType().equals(Material.AIR)) continue;
                        for(ItemStack i : hearts.keySet()){
                            if(item.equals(i)){
                                player.getInventory().removeItem(item);
                                player.getInventory().addItem(hearts.get(i));
                                convertedItems++;
                            }
                        }
                    }
                    player.sendMessage("Se han convertido " + convertedItems + " items");
                });
    }

    static CommandAPICommand FromInverted(){
        return new CommandAPICommand("undo")
                .withPermission("delta.items")
                .executesPlayer((player, args) -> {
                    int convertedItems = 0;
                    for(ItemStack item : player.getInventory().getContents()){
                        if(item == null) continue;
                        if(item.getType().equals(Material.AIR)) continue;
                        for(ItemStack i : Invertedhearts.keySet()){
                            if(item.equals(i)){
                                player.getInventory().removeItem(item);
                                player.getInventory().addItem(Invertedhearts.get(i));
                                convertedItems++;
                            }
                        }
                    }
                    player.sendMessage("Se han convertido " + convertedItems + " items");
                });
    }
}
