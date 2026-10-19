package org.egymouse.speedBuilders.listeners;

import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockDamageEvent;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.PlayerData;

import java.util.UUID;

public class InstantBreakListener implements Listener {

    @EventHandler
    public void onBlockDamage(BlockDamageEvent event) {
        // 1. 仅对生存模式玩家生效
        if (event.getPlayer().getGameMode() != GameMode.SURVIVAL) {
            return;
        }

        if (GameLoop.currentPhase == GameLoop.GamePhase.JUDGE) {
            event.setCancelled(true);
            return;
        }

        // 1. 从事件中获取方块
        Block block = event.getBlock();

        if (!isInBuildArea(event.getPlayer(), block.getLocation())) {
            event.setCancelled(true);
            return;
        }

        // 2. 获取方块的材质 (Material)
        Material material = block.getType();

        // 3. 获取硬度 (getHardness 返回 float)
        // 注意：此方法需要较新版本的 Spigot/Paper API (1.20.5+)
        float hardness = material.getHardness();

        if (hardness == 0) return;

        // 4. 核心：设置为瞬间破坏
        event.setInstaBreak(true);  //todo 修复秒破无挖掘延迟的bug


//        event.getBlock().setType(Material.AIR);
    }

    public static boolean isInBuildArea(Player player, Location location) {
        UUID uuid = player.getUniqueId();
        PlayerData playerData = GameLoop.player_data.get(uuid);
        if (playerData == null) return false;
        int offset = playerData.getOffset();
        location = GameLoop.offset(location, -offset);  //反过来在指定位置 左加右减
        int x = location.getBlockX();
        int y = location.getBlockY();
        int z = location.getBlockZ();
        if (x >= -3 && x <= 3 && y >= -60 && y <= -54 && z >= -3 && z <= 3) return true;
        return false;
    }
}
