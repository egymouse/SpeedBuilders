package org.egymouse.speedBuilders;

import org.bukkit.GameMode;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class MyListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        // 1. 仅对生存模式玩家生效
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        // 4. 核心：设置为瞬间破坏
        event.setInstaBreak(true);
    }
}
