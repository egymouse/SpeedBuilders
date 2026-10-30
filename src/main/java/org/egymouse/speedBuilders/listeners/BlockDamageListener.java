package org.egymouse.speedBuilders.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.*;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.inventory.ItemStack;
import org.egymouse.speedBuilders.*;

import java.util.UUID;

public class BlockDamageListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        Block block = event.getBlock();
        Material type = block.getType();

        boolean doDrop = true;

        // 1. 仅对生存模式玩家生效
        if (player.getGameMode() != GameMode.SURVIVAL) {
            return;
        }
        if (ToolClass.destroy_tick.containsKey(uuid) && !GameLoop.noCooldown) {
            event.setCancelled(true);
            return;
        }
        else if (!BlockDamageListener.isInBuildArea(player, block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        else if (GameLoop.player_data.containsKey(uuid)) {
            if (GameLoop.currentPhase == GameLoop.GamePhase.JUDGE) {
                event.setCancelled(true);
                return;
            }
            else if (GameLoop.currentPhase == GameLoop.GamePhase.DISPLAY) {
                if (GameLoop.allowBreak) {
                    doDrop = false;
                }
                else {
                    event.setCancelled(true);
                    return;
                }

            }
        }
        else if (TrainManager.player_trainData.containsKey(uuid)) {
            if (TrainManager.player_trainData.get(uuid).currentPhase == GameLoop.GamePhase.DISPLAY) {
                doDrop = false;
            }
        }
        else {
            event.setCancelled(true);
            return;
        }



        int dropAmount = 1;

        BlockData blockData = block.getBlockData();  //todo 增加监听物品产生（最好是监听依附物掉落）如果不是build就取消
        if (blockData instanceof Slab) {
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

    public static boolean isInBuildArea(Player player, Location location) {  //todo 改成返回3个bool值，一个是是否可以破坏，一个是破坏是否会掉落，一个是是否可以放置
        UUID uuid = player.getUniqueId();
        PlayerData playerData = GameLoop.player_data.get(uuid);
        TrainPlayerData trainPlayerData = TrainManager.player_trainData.get(uuid);
        int offset;
        Location arenaLocation;
        if (playerData != null) {
            offset = playerData.getOffset();
            arenaLocation = GameLoop.BUILD_LOCATION;
        }
        else if (trainPlayerData != null) {
            offset = trainPlayerData.getOffset();
            arenaLocation = GameLoop.TRAIN_LOCATION;
        }
        else {
            return false;
        }

        arenaLocation = GameLoop.offset(arenaLocation, offset);  //反过来在指定位置 左加右减
        int minX = arenaLocation.getBlockX();
        int minY = arenaLocation.getBlockY();
        int minZ = arenaLocation.getBlockZ();
        int maxX = minX + 6;
        int maxY = minY + 6;
        int maxZ = minZ + 6;

        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (x >= minX && x <= maxX && y >= minY && y <= maxY && z >= minZ && z <= maxZ) return true;
        return false;
    }
}
