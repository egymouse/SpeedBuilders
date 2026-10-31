package org.egymouse.speedBuilders.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.MultipleFacing;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockPhysicsEvent;
import org.bukkit.inventory.ItemStack;
import org.egymouse.speedBuilders.BlockEdit;

public class PhysicsListener implements Listener {

    @EventHandler(priority = EventPriority.LOWEST, ignoreCancelled = true)
    public void onBlockPhysics(BlockPhysicsEvent event) {

        if (BlockEdit.noBlockPhysics) {
            event.setCancelled(true);
            return;
        }
        if (true) return;
        // 1. 检查是否是你关心的“依赖方块”（例如按钮、火把等）
        // 也可以检查 event.getSourceBlock()，即触发更新的方块
        Material type = event.getBlock().getType();
//        if (isProtectedBlock(event.getBlock())) {
//            event.setCancelled(true); // 尝试取消物理更新
//            // 只处理火把，可按需扩展
//
//        }

        if (type == Material.TORCH ||
                type == Material.WALL_TORCH ||
                type == Material.REDSTONE_TORCH ||
                type == Material.REDSTONE_WALL_TORCH) {

            // 取消物理更新，阻止它因为失去附着方块而掉落
            event.setCancelled(true);
        }
//        event.setCancelled(true);

//        event.setCancelled(true);
    }

    private boolean isProtectedBlock(org.bukkit.block.Block block) {
        // 在这里定义哪些方块需要被保护，避免影响正常游戏
        // 例如：Material.LEVER, Material.STONE_BUTTON, Material.TORCH 等
        if (block.getType().name().endsWith("BUTTON")){
            Bukkit.broadcastMessage(block.getType().name() + " true");
            return true;
        }
        if (block.getType().name().endsWith("TORCH")) {  //WALL_TORCH
            Bukkit.broadcastMessage(block.getType().name() + " true");
            return true;
        }
        Bukkit.broadcastMessage(block.getType().name() + " false");
        return false;
    }


    @EventHandler
    public void onBlockPhysics2(BlockPhysicsEvent event) {
        if (event.getBlock().getType() != Material.VINE) {
            return;

        }
//        event.setCancelled(true);


        dropVineWhenBroken(event.getBlock());
    }

    private void dropVineWhenBroken(Block block) {
        if (block.getType() != Material.VINE) {
            return;
        }
        if (hasVineSupport(block)) {
            return;
        }
        World world = block.getWorld();
        Location location = block.getLocation();
        block.breakNaturally();
        if (block.getType() == Material.VINE) {
            block.setType(Material.AIR);
        }
        world.dropItemNaturally(location, new ItemStack(Material.VINE));
    }

    private boolean hasVineSupport(Block block) {
        if (block.getType() != Material.VINE) {
            return true;
        }
        MultipleFacing multipleFacing = (MultipleFacing) block.getBlockData();
        BlockFace[] faces = new BlockFace[]{BlockFace.NORTH, BlockFace.SOUTH, BlockFace.EAST, BlockFace.WEST, BlockFace.UP};
        for (BlockFace face : faces) {
            if (multipleFacing.hasFace(face)) {
                if (block.getRelative(face).getType().isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }
}