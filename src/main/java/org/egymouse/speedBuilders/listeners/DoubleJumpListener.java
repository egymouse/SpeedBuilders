package org.egymouse.speedBuilders.listeners;

import org.bukkit.GameMode;
import org.bukkit.Sound;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.event.player.PlayerToggleFlightEvent;
import org.bukkit.util.Vector;

import java.util.*;

public class DoubleJumpListener implements Listener {

    // 存储哪些玩家当前“可跳”（已经起跳、尚未落地）
    private final Set<UUID> airborne = new HashSet<>();

    @EventHandler
    public void onMove(PlayerMoveEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // 检测玩家是否着地：脚下方块不是空气
        Block blockBelow = player.getLocation().subtract(0, 1, 0).getBlock();
        boolean onGround = blockBelow.getType().isSolid();

        if (onGround) {
            // 着地时，重新允许飞行，并移出“空中”集合
            player.setAllowFlight(true);
            airborne.remove(player.getUniqueId());
        } else {
            // 离地时，标记为“空中”（代表已经消耗了一次原版跳跃）
            // 但只有在玩家确实起跳后才标记，避免边缘掉落误判
            if (player.getVelocity().getY() > -0.1) {
                airborne.add(player.getUniqueId());
            }
        }
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onToggleFlight(PlayerToggleFlightEvent event) {
        Player player = event.getPlayer();
        if (player.getGameMode() == GameMode.CREATIVE) return;

        // 核心：立即取消飞行切换
        event.setCancelled(true);
        player.setFlying(false);
        player.setAllowFlight(false); // 消耗掉本次“可飞”状态

        // 只在“空中”状态下才允许二段跳，防止在地面双击触发
        if (!airborne.contains(player.getUniqueId())) return;

        // 冷却检查（见下文）
        if (isOnCooldown(player)) return;

        // 施加冲量
        Vector direction = player.getLocation().getDirection();
        Vector impulse = direction.multiply(0.6).setY(0.9); // 向前 + 向上
        player.setVelocity(player.getVelocity().add(impulse));
        // 如果想让冲量完全替换原速度，直接 player.setVelocity(impulse) 即可

        // 触发后移出“空中”集合，防止同一段空中多次触发
        airborne.remove(player.getUniqueId());
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