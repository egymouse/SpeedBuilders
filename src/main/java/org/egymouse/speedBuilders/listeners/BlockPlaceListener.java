package org.egymouse.speedBuilders.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPlaceEvent;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.TrainManager;

public class BlockPlaceListener implements Listener {

    @EventHandler
    public void onBlockPlace(BlockPlaceEvent event) {
        // 1. 仅对生存模式玩家生效
        Player player = event.getPlayer();
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }

//        if (GameLoop.currentPhase != GameLoop.GamePhase.BUILD) {
//            event.setCancelled(true);
//            return;
//        }

        // 1. 从事件中获取方块
        Block block = event.getBlock();

        if (!BlockDamageListener.isInBuildArea(event.getPlayer(), block.getLocation())) {
            event.setCancelled(true);
            return;
        }
        else if (GameLoop.player_data.containsKey(player.getUniqueId())) {
            if (GameLoop.currentPhase != GameLoop.GamePhase.BUILD) {
                event.setCancelled(true);
                return;
            }
        }
        else if (TrainManager.player_trainData.containsKey(player.getUniqueId())) {
            if (TrainManager.player_trainData.get(player.getUniqueId()).getCurrentPhase() == GameLoop.GamePhase.DISPLAY) {
                event.setCancelled(true);
            }
        }


//        event.getBlock().setType(Material.AIR);
    }

}
