package org.egymouse.speedBuilders.listeners;

import org.bukkit.GameMode;
import org.bukkit.block.BlockFace;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Directional;
import org.bukkit.block.data.type.Door;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.inventory.ItemStack;


import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.Bisected;

public class AutoPickupListener implements Listener {

//    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
//    public void onBlockBreak(BlockBreakEvent e) {
//        // 1. 关闭该方块的默认掉落
//        e.setDropItems(false);
//
//        Player player = e.getPlayer();
//        PlayerInventory inv = player.getInventory();
//
//        // 2. 获取考虑了手中工具附魔(如时运)的真实掉落物
//        e.getBlock().getDrops(inv.getItemInMainHand()).forEach(stack -> {
//            // 3. 将物品添加到背包，返回值为未能放入的物品(如果背包已满)
//            var leftover = inv.addItem(stack);
//
//            // 4. 处理背包已满的情况：将放不下的物品掉落在方块位置
//            leftover.values().forEach(item ->
//                e.getBlock().getWorld().dropItemNaturally(e.getBlock().getLocation(), item)
//            );
//        });
//    }

    @EventHandler(priority = EventPriority.HIGHEST, ignoreCancelled = true)
    public void onBlockBreak(BlockBreakEvent e) {
        if (e.getPlayer().getGameMode() == GameMode.CREATIVE ) return;
        Block block = e.getBlock();
        Material type = block.getType();
        BlockData data = block.getBlockData();

        // 取消原版掉落，避免再生成默认掉落物
        e.setDropItems(false);

        // 不是物品的方块无法“掉自身”，直接不掉
        if (type.isAir() || !type.isItem()) {
            return;
        }

        // 1. 处理床 (Bed)
        if (data instanceof Bed) {
            Bed bed = (Bed) data;
            BlockFace facing = bed.getFacing();
            // 床的“头”和“脚”是沿着 facing 方向相邻的
            Block otherHalf = block.getRelative(bed.getPart() == Bed.Part.HEAD ? facing.getOppositeFace() : facing);

            if (otherHalf.getBlockData() instanceof Bed) {
                // 手动将另一半设置为空气，同时避免再次触发物理事件
                otherHalf.setBlockData(Material.AIR.createBlockData(), false);
            }
        }
        // 2. 处理门 (Door)
        else if (data instanceof Door) {
            // 门是垂直方向上的两格
            Block otherHalf = block.getRelative(BlockFace.UP); // 假设你挖的是下半部分
            if (!(otherHalf.getBlockData() instanceof Door)) {
                otherHalf = block.getRelative(BlockFace.DOWN); // 如果上面不是门，那就在下面
            }

            if (otherHalf.getBlockData() instanceof Door) {
                otherHalf.setBlockData(Material.AIR.createBlockData(), false);
            }
        }
        // 关键：方块是什么就生成什么，完全不参考玩家手中物品
        ItemStack drop = new ItemStack(type, 1);

        // 直接进背包；返回值是背包放不下的部分
        var leftover = e.getPlayer().getInventory().addItem(drop);

        // 背包满了就掉在方块原位置，避免吞物品
        leftover.values().forEach(item ->
                block.getWorld().dropItemNaturally(block.getLocation(), item)
        );
    }



//    public class BlockSelfDropListener implements Listener {
//
//
//    }
}