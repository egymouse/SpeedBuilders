package org.egymouse.speedBuilders.listeners;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;

public class PhysicsListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {
        // 1. 检查是否是你关心的“依赖方块”（例如按钮、火把等）
        // 也可以检查 event.getSourceBlock()，即触发更新的方块
        if (isProtectedBlock(event.getBlock())) {
            event.setCancelled(true); // 尝试取消物理更新
        }

//        event.setCancelled(true);
    }

    private boolean isProtectedBlock(org.bukkit.block.Block block) {
        // 在这里定义哪些方块需要被保护，避免影响正常游戏
        // 例如：Material.LEVER, Material.STONE_BUTTON, Material.TORCH 等
        if (block.getType().name().endsWith("BUTTON")) return true;
        if (block.getType() == Material.TORCH) return true;
        return false;
    }
}