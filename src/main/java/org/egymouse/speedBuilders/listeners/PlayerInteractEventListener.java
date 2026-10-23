package org.egymouse.speedBuilders.listeners;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.Action;
import org.bukkit.event.player.PlayerInteractEvent;
import org.bukkit.inventory.EquipmentSlot;
import org.bukkit.inventory.ItemStack;
import org.bukkit.persistence.PersistentDataContainer;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.scheduler.BukkitScheduler;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.SpeedBuilders;
import org.egymouse.speedBuilders.ToolClass;

import java.util.UUID;

public class PlayerInteractEventListener implements Listener {
    @EventHandler
    public void run(PlayerInteractEvent e){
        Player player = e.getPlayer();
        ItemStack itemStack = e.getItem();
         if((e.getAction() == Action.RIGHT_CLICK_AIR || e.getAction() == Action.RIGHT_CLICK_BLOCK) &&  (e.getHand() == EquipmentSlot.HAND)){
            if(e.getItem()!=null){  //只能改成offhand，事件执行的时候玩家其实还没interact，瞬间替换物品导致玩家发现新物品又interact，然后切换回去重复了就不Interact了
                //不对应该是延后1tick
//                Bukkit.broadcastMessage(itemStack.getType().name());
//                Bukkit.broadcastMessage(player.getInventory().getItemInMainHand().getType().name());
                BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
                bukkitScheduler.runTaskLater(SpeedBuilders.getPlugin(), () -> {
//                    Bukkit.broadcastMessage(itemStack.getType().name());
//                    Bukkit.broadcastMessage(player.getInventory().getItemInMainHand().getType().name());

                    if(e.getItem().getType() == Material.MAGMA_CREAM){
                        if(ToolClass.playing){
                            e.getPlayer().sendMessage(ChatColor.RED + "游戏已经开始，请稍后");
                            return;
                        }
                        if(!ToolClass.wait_player.contains(e.getPlayer())){
                            ToolClass.wait_player.add(e.getPlayer());
                            e.getPlayer().getInventory().clear();
                            e.getPlayer().sendMessage(ChatColor.GREEN + "你进入了游玩队列");
                            Bukkit.broadcastMessage("游玩队列人数" + ToolClass.wait_player.size());
                            e.getPlayer().getInventory().setItem(1,ToolClass.cancel);
                            e.getPlayer().getInventory().setItem(0,ToolClass.start);
//                            Bukkit.broadcastMessage(Bukkit.getCurrentTick() + "");
//                            Bukkit.broadcastMessage(e.getAction().toString());
                        }else{
                            e.getPlayer().sendMessage(ChatColor.RED + "你已经在游玩队列当中");
                        }

                    }
                    else if(e.getItem().getType() == Material.STICK){
                        if(ToolClass.wait_player.contains(e.getPlayer())){
                            ToolClass.wait_player.remove(e.getPlayer());
                            e.getPlayer().getInventory().clear();
                            e.getPlayer().sendMessage(ChatColor.GREEN + "你退出了游玩队列");
                            Bukkit.broadcastMessage("游玩队列人数" + ToolClass.wait_player.size());
                            e.getPlayer().getInventory().setItem(0,ToolClass.create);
                            e.getPlayer().getInventory().setItem(1,ToolClass.play);
//                            Bukkit.broadcastMessage(Bukkit.getCurrentTick() + "");
//                            Bukkit.broadcastMessage(e.getAction().name());
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

                }, 1);
                //                if (e.getItem().getType() == Material.COAL) {
//                    player.getInventory().setItemInMainHand(new ItemStack(Material.IRON_INGOT));
//                }
//                else if (itemStack.getType() == Material.IRON_INGOT) {
//                    player.getInventory().setItemInMainHand(new ItemStack(Material.GOLD_INGOT));
//                }
//                else if (itemStack.getType() == Material.GOLD_INGOT) {
//                    player.getInventory().setItemInMainHand(new ItemStack(Material.DIAMOND));
//                }
//                else if (itemStack.getType() == Material.DIAMOND) {
//                    player.getInventory().setItemInMainHand(new ItemStack(Material.NETHERITE_INGOT));
//                }

            }
        }

    }


    @EventHandler
    public void onPlayerLeftClick(PlayerInteractEvent event) {
        if (event.getHand() != EquipmentSlot.HAND) return;
        if (event.getAction() != Action.LEFT_CLICK_BLOCK) return;
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = event.getClickedBlock();
        Material type = block.getType();

        // 1. 仅对生存模式玩家生效
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (GameLoop.currentPhase == GameLoop.GamePhase.JUDGE) {
            event.setCancelled(true);
            return;
        }
        else if (!BlockDamageListener.isInBuildArea(player, block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        else if (ToolClass.destroy_tick.containsKey(uuid)) {
            event.setCancelled(true);
            return;
        }


        int dropAmount = 1;
        boolean doDrop = true;
        BlockData blockData = block.getBlockData();
        if (GameLoop.currentPhase == GameLoop.GamePhase.DISPLAY) {  //todo 增加监听物品产生（最好是监听依附物掉落）如果不是build就取消
            doDrop = false;
        }
        else if (blockData instanceof Slab) {
            Slab slab = (Slab) blockData;
            if (slab.getType() == Slab.Type.DOUBLE) {
                dropAmount = 2;
            }
        }
        else if(blockData instanceof Bed b){
            if(b.getPart() != Bed.Part.HEAD){
                doDrop = false;
            }
        }else if(blockData instanceof Bisected b){
            if(blockData instanceof Stairs || blockData instanceof TrapDoor || blockData instanceof SmallDripleaf){

            }else{
                if(b.getHalf() != Bisected.Half.BOTTOM){
                    doDrop = false;
                }
            }

        }

        if (doDrop) {
            // 关键：方块是什么就生成什么，完全不参考玩家手中物品
            ItemStack drop = new ItemStack(type, dropAmount);

            // 直接进背包；返回值是背包放不下的部分
            var leftover = event.getPlayer().getInventory().addItem(drop);

            // 背包满了就掉在方块原位置，避免吞物品
            leftover.values().forEach(item ->
                    block.getWorld().dropItemNaturally(block.getLocation(), item)
            );
        }



        block.setType(Material.AIR);
        ToolClass.destroy_tick.put(uuid,2);

        event.setCancelled(true);

    }


}
