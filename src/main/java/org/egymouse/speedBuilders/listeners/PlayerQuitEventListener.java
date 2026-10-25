package org.egymouse.speedBuilders.listeners;

import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;
import org.egymouse.speedBuilders.TrainManager;

public class PlayerQuitEventListener implements Listener {

    @EventHandler
    public void onPlayerQuit(PlayerQuitEvent event) {
        Player player = event.getPlayer();
        TrainManager.tryUnregisterPlayer(player);
    }
}
