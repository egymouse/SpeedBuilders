package org.egymouse.speedBuilders.listeners;

import org.bukkit.GameMode;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;

public class InstantBreakListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        // 1. 仅对生存模式玩家生效
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        // 1. 从事件中获取方块
        Block block = event.getBlock();

        // 2. 获取方块的材质 (Material)
        Material material = block.getType();

        // 3. 获取硬度 (getHardness 返回 float)
        // 注意：此方法需要较新版本的 Spigot/Paper API (1.20.5+)
        float hardness = material.getHardness();

//        if (hardness == 0) return;

        // 4. 核心：设置为瞬间破坏
        event.setInstaBreak(true);  //todo 修复秒破无挖掘延迟的bug
    }
}
