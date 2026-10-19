package org.egymouse.speedBuilders;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.bukkit.Bukkit;
import org.bukkit.GameMode;
import org.bukkit.GameRule;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.egymouse.speedBuilders.commands.TestCommand;
import org.egymouse.speedBuilders.listeners.DoubleJumpListener;

public final class SpeedBuilders extends JavaPlugin {


    public static SpeedBuilders plugin;

    public static SpeedBuilders getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        // Plugin startup logic
        plugin = this;

        registerAllListeners();
        getCommand("test").setExecutor(new TestCommand());
        World world = Bukkit.getWorld("world");
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);

        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                if (player.getGameMode() == GameMode.CREATIVE) return;
                boolean onGround = DoubleJumpListener.isPlayerOnGroundRayTrace(player);
                if (onGround) {
                    // 着地时，重新允许飞行，并移出“空中”集合
                    player.setAllowFlight(true);
                } else {
                    // 离地时，标记为“空中”（代表已经消耗了一次原版跳跃）
                    // 但只有在玩家确实起跳后才标记，避免边缘掉落误判
                    if (player.getVelocity().getY() > -0.2) {

                    }
                    else {
                        player.setAllowFlight(false);
                    }
                }
            }
        }, 1, 1);


//        getServer().getPluginManager().registerEvents(new InstantBreakListener(), this);
    }


    public void registerAllListeners() {
        try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
            // 获取所有实现了 Listener 接口的类
            ClassInfoList listenerClasses = scanResult.getClassesImplementing(Listener.class);

            for (Class<?> clazz : listenerClasses.loadClasses()) {
                try {
                    Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
                    getServer().getPluginManager().registerEvents(listener, this);
                } catch (Exception e) {
                    getLogger().warning("实例化监听器失败: " + clazz.getName());
                }
            }
        }
    }

//    public void registerAllCommands() {
//        try (ScanResult scanResult = new ClassGraph().enableAllInfo().scan()) {
//            // 获取所有实现了 Listener 接口的类
//            ClassInfoList listenerClasses = scanResult.getClassesImplementing(CommandExecutor.class);
//
//            for (Class<?> clazz : listenerClasses.loadClasses()) {
//                try {
//                    Listener listener = (Listener) clazz.getDeclaredConstructor().newInstance();
//                    getServer().getPluginManager().registerEvents(listener, this);
//                } catch (Exception e) {
//                    getLogger().warning("实例化监听器失败: " + clazz.getName());
//                }
//            }
//        }
//    }

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
