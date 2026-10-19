package org.egymouse.speedBuilders.listeners;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.ToolClass;

public class PlayerInteractEventListener implements Listener {
        @EventHandler
        public void run(PlayerInteractEvent e){
            Player player = e.getPlayer();
            if((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) && (e.getHand() != EquipmentSlot.OFF_HAND)){
                if(e.getItem()!=null){
                    if(e.getItem().getType() == Material.MAGMA_CREAM){
                        if(ToolClass.playing){
                            e.getPlayer().sendMessage(ChatColor.RED + "游戏已经开始，请稍后");
                            return;
                        }
                        if(!ToolClass.wait_player.contains(e.getPlayer())){
                            ToolClass.wait_player.add(e.getPlayer());
                            e.getPlayer().getInventory().clear();
                            e.getPlayer().sendMessage(ChatColor.GREEN + "你进入了游玩队列");
                            e.getPlayer().getInventory().setItem(1,ToolClass.cancel);
                            e.getPlayer().getInventory().setItem(0,ToolClass.start);
                        }else{
                            e.getPlayer().sendMessage(ChatColor.RED + "你已经在游玩队列当中");
                        }

                    }
                    else if(e.getItem().getType() == Material.STICK){
                        if(ToolClass.wait_player.contains(e.getPlayer())){
                            ToolClass.wait_player.remove(e.getPlayer());
                            e.getPlayer().getInventory().clear();
                            e.getPlayer().sendMessage(ChatColor.GREEN + "你退出了游玩队列");
                            e.getPlayer().getInventory().setItem(0,ToolClass.create);
                            e.getPlayer().getInventory().setItem(1,ToolClass.play);
                        }else{
                            e.getPlayer().sendMessage(ChatColor.RED + "你没在游玩队列当中");
                        }

                    }
                    else if(e.getItem().getType() == Material.EMERALD){
                        if(ToolClass.wait_player.contains(e.getPlayer())){
                            e.getPlayer().getInventory().clear();
                            Bukkit.broadcastMessage(player.getName() + "开始了游戏");
                            GameLoop.start();
                        }else{
                            e.getPlayer().sendMessage(ChatColor.RED + "你没在游玩队列当中");
                        }

                    }

                }
            }



        }

}
