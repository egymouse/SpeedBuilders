package org.egymouse.speedBuilders.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.bukkit.event.block.BlockPlaceEvent;
import org.egymouse.speedBuilders.GameLoop;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // 1. 仅对生存模式玩家生效
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (GameLoop.currentPhase != GameLoop.GamePhase.BUILD) {
            event.setCancelled(true);
            return;
        }

        // 1. 从事件中获取方块
        Block block = event.getBlock();

        if (!InstantBreakListener.isInBuildArea(event.getPlayer(), block.getLocation())) {
            event.setCancelled(true);
            return;
        }


//        event.getBlock().setType(Material.AIR);
    }

}
