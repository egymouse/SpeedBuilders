package org.egymouse.speedBuilders.listeners;

import org.bukkit.entity.Entity;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDamageEvent;
import org.bukkit.event.entity.EntityExhaustionEvent;

public class PlayerStatusListener implements Listener {

    @EventHandler
    public void onEntityDamage(EntityDamageEvent event) {
        Entity entity = event.getEntity();
        if (entity instanceof Player) {
            event.setCancelled(true);
        }
    }

    @EventHandler
    public void onEntityExhaustion(EntityExhaustionEvent event) {
        // 确保事件由玩家触发
        if (!(event.getEntity() instanceof Player player)) return;


        event.setCancelled(true);
    }
}
