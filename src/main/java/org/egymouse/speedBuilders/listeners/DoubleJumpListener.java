package org.egymouse.speedBuilders.listeners;

import com.destroystokyo.paper.event.player.PlayerJumpEvent;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.BoundingBox;
import org.bukkit.util.RayTraceResult;
import org.bukkit.util.Vector;

import java.util.*;

public class DoubleJumpListener implements Listener {

    // 存储哪些玩家当前“可跳”（已经起跳、尚未落地）
    private final Set<UUID> airborne = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;
        if (true) return;

        // 检测玩家是否着地：脚下方块不是空气
        Block blockBelow = player.getLocation().subtract(0, 0.1, 0).getBlock();  //方块边缘就不行
        boolean onGround = blockBelow.getType().isSolid();
        onGround = isPlayerOnGroundRayTrace(player);
//        onGround = player.isOnGround();   //我相信你客户端不会spoof的

        if (onGround) {
            // 着地时，重新允许飞行，并移出“空中”集合
            player.setAllowFlight(true);
            airborne.remove(player.getUniqueId());
        } else {
            // 离地时，标记为“空中”（代表已经消耗了一次原版跳跃）
            // 但只有在玩家确实起跳后才标记，避免边缘掉落误判
            if (player.getVelocity().getY() > -0.2) {
                airborne.add(player.getUniqueId());
            }
            else {
                player.setAllowFlight(false);
            }
        }
    }

    @EventHandler
    public void onPlayerJump(PlayerJumpEvent event) {
        if (true) return;
        Player player = event.getPlayer();
        // 玩家跳跃时触发
        event.getPlayer().sendMessage("你跳了一下！");
        if (player.getGameMode() == GameMode.CREATIVE) return;
        player.setAllowFlight(true);
    }


    public static boolean isPlayerOnGroundRayTrace(Player player) {
        BoundingBox boundingBox = player.getBoundingBox(); // 获取玩家碰撞箱
        World world = player.getWorld();

        // 定义射线起点（碰撞箱的四个底角）
        double minX = boundingBox.getMinX();
        double maxX = boundingBox.getMaxX();
        double minZ = boundingBox.getMinZ();
        double maxZ = boundingBox.getMaxZ();
        double minY = boundingBox.getMinY();

        Location[] corners = {
                new Location(world, minX, minY, minZ), // 角1
                new Location(world, minX, minY, maxZ), // 角2
                new Location(world, maxX, minY, minZ), // 角3
                new Location(world, maxX, minY, maxZ)  // 角4
        };

        Vector down = new Vector(0, -1, 0); // 向下方向向量
        double maxDistance = 0.1; // 最大检测距离稍大于0，足以触碰到脚下方块

        for (Location corner : corners) {
            RayTraceResult result = world.rayTraceBlocks(
                    corner, down, maxDistance
            );
            // 如果射线击中了方块，且该方块不是空气/液体，则认为玩家站在地面上
            if (result != null && result.getHitBlock() != null) {
                Material type = result.getHitBlock().getType();
                if (type.isSolid()) {
                    return true;
                }
            }
        }
        return false;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {  //todo 修复它总是有点卡卡的 因为客户端以为自己能飞
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;

        // 核心：立即取消飞行切换
        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(false); // 消耗掉本次“可飞”状态

        // 只在“空中”状态下才允许二段跳，防止在地面双击触发
//        if (!airborne.contains(player.getUniqueId())) return;

        // 冷却检查（见下文）
//        if (isOnCooldown(player)) return;

        // 施加冲量
        Vector direction = player.getLocation().getDirection();
        direction = new Vector(0, 1, 0);
        Vector impulse = direction.multiply(0.6).setY(1.2); // 向前 + 向上  //y原本是0.9
        player.setVelocity(player.getVelocity().add(impulse));
        // 如果想让冲量完全替换原速度，直接 player.setVelocity(impulse) 即可

        // 触发后移出“空中”集合，防止同一段空中多次触发
//        airborne.remove(player.getUniqueId());
        markCooldown(player);

        // 可选：播放效果
        player.getWorld().playSound(player.getLocation(), Sound.ENTITY_BLAZE_SHOOT, 1f, 1f);
    }


    private final Map<UUID, Long> cooldowns = new HashMap<>();
    private static final long COOLDOWN_MS = 3000; // 3秒

    private boolean isOnCooldown(Player player) {
        Long last = cooldowns.get(player.getUniqueId());
        return last != null && (System.currentTimeMillis() - last) < COOLDOWN_MS;
    }

    private void markCooldown(Player player) {
        cooldowns.put(player.getUniqueId(), System.currentTimeMillis());
    }
}