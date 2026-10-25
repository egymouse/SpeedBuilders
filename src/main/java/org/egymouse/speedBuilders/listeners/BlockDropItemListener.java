package org.egymouse.speedBuilders.listeners;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.entity.Item;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDropItemEvent;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.TrainManager;

import java.util.List;

public class BlockDropItemListener implements Listener {
    @EventHandler
    public void onBlockDrop(BlockDropItemEvent event) {  //todo 改成blockDestroyEvent 哎我就不改 我等szm强烈要求我再改  毕竟这个浪费世界优化的很少，不如新增一些简单的对体验优化大的功能
//        event.setCancelled(true);
        Player player = event.getPlayer();
        Block block = event.getBlock();
        Location location = block.getLocation();
        List<Item> items = event.getItems();
        if (player == null) {return;}
        if (!items.isEmpty()) {
            if (!BlockDamageListener.isInBuildArea(player, location)) {
                event.getItems().clear();
                return;
            }
            else if (GameLoop.player_data.containsKey(player.getUniqueId())) {
                if (GameLoop.currentPhase != GameLoop.GamePhase.BUILD) {
                    event.getItems().clear();
                    return;
                }
            }
            else if (TrainManager.player_trainData.containsKey(player.getUniqueId())) {
                if (TrainManager.player_trainData.get(player.getUniqueId()).getCurrentPhase() == GameLoop.GamePhase.DISPLAY) {
                    event.getItems().clear();
                    return;
                }
            }



//            Bukkit.broadcastMessage(items.getFirst().getItemStack().getType().name() + " debug_21");
            //todo 解决神秘方块掉落物品问题
        }
//        event.getItems().clear();


//        // 例如：检查被破坏的方块是否是钻石矿石
//        if (event.getBlockState().getType() == Material.DIAMOND_ORE) {
//            // 清空所有原版掉落物，你可以手动添加你自己的掉落
//            event.getItems().clear();
//            // 然后可以在这里手动生成你想要的物品
//            // event.getBlock().getWorld().dropItemNaturally(...);
//        }
    }
}