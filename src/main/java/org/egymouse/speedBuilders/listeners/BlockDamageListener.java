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
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.PlayerData;
import org.egymouse.speedBuilders.ToolClass;

import java.util.UUID;

public class BlockDamageListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        Player player = event.getPlayer();
        UUID uuid = player.getUniqueId();
        // 1. 从事件中获取方块
        Block block = event.getBlock();
        Material type = block.getType();



        // 1. 仅对生存模式玩家生效
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (GameLoop.currentPhase == GameLoop.GamePhase.JUDGE) {
            event.setCancelled(true);
            return;
        }
        else if (!isInBuildArea(event.getPlayer(), block.getLocation())) {
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



        event.getBlock().setType(Material.AIR);
        ToolClass.destroy_tick.put(uuid,2);



        // 4. 核心：设置为瞬间破坏
        event.setInstaBreak(true);  //todo 修复秒破无挖掘延迟的bug
        event.setCancelled(true);


//        event.getBlock().setType(Material.AIR);
    }

    public static boolean isInBuildArea(Player player, Location location) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = GameLoop.player_data.get(uuid);
        if (playerData == null) return false;
        int offset = playerData.getOffset();
        location = GameLoop.offset(location, -offset);  //反过来在指定位置 左加右减
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (x >= -3 && x <= 3 && y >= -60 && y <= -54 && z >= -3 && z <= 3) return true;
        return false;
    }
}
