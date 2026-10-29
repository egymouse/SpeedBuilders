package org.egymouse.speedBuilders.listeners;

import org.bukkit.entity.EntityType;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;

public class WitherSpawnListener implements Listener {
    @EventHandler
    public void onCreatureSpawn(CreatureSpawnEvent event) {
        if (isPreventSpawn(event.getSpawnReason())) {
            event.setCancelled(true);
            return;
        }
    }


    public boolean isPreventSpawn(CreatureSpawnEvent.SpawnReason reason) {
        if (reason == CreatureSpawnEvent.SpawnReason.BUILD_WITHER) return false;
        if (reason == CreatureSpawnEvent.SpawnReason.BUILD_COPPERGOLEM) return false;
        if (reason == CreatureSpawnEvent.SpawnReason.BUILD_IRONGOLEM) return false;
        if (reason == CreatureSpawnEvent.SpawnReason.BUILD_SNOWMAN) return false;
        return true;
    }
}