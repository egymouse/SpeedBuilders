package org.egymouse.speedBuilders.listeners;

import org.bukkit.Bukkit;
import org.bukkit.entity.Item;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;

import java.util.List;

public class BlockDropItemListener implements Listener {
    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {
        List<Item> items = event.getItems();
        event.getItems().clear();
        if (!items.isEmpty()) {
            Bukkit.broadcastMessage(items.toString() + " debug_1");
            //todo 解决神秘方块掉落物品问题
        }

//        // 例如：检查被破坏的方块是否是钻石矿石
//        if (event.getBlockState().getType() == Material.DIAMOND_ORE) {
//            // 清空所有原版掉落物，你可以手动添加你自己的掉落
//            event.getItems().clear();
//            // 然后可以在这里手动生成你想要的物品
//            // event.getBlock().getWorld().dropItemNaturally(...);
//        }
    }
}