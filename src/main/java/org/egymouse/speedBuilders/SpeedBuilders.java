package org.egymouse.speedBuilders;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.SmallDripleaf;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.event.Listener;
import org.bukkit.inventory.ItemStack;
import org.bukkit.plugin.java.JavaPlugin;
import org.bukkit.scheduler.BukkitScheduler;
import org.egymouse.speedBuilders.commands.EndCommand;
import org.egymouse.speedBuilders.commands.MergeCommand;
import org.egymouse.speedBuilders.commands.TestCommand;
import org.egymouse.speedBuilders.listeners.DoubleJumpListener;

import java.util.Map;
import java.util.UUID;

public final class SpeedBuilders extends JavaPlugin {


    public static SpeedBuilders plugin;

    public static SpeedBuilders getPlugin() {
        return plugin;
    }

    @Override
    public void onEnable() {
        //todo 挖掘延迟(客户端式修复)， 初始化玩家arena（用structure且记录大小) 玩家下线逻辑 破坏下面花草会导致花草不给回玩家  右键多次右键(直接把核心问题解决了） 按钮掉落又出现了（核心是区分实际掉落和依附掉落)
        // 办公室给予的物品出错解决 玩家死亡移除平台 火把不能控制不触发physics(只能新增自定义事件解决)
        // 添加练习模式 自定义物品栏顺序 更好的出题（修改地板变方便那种）  记录玩家最优时间  大跳手感不好 修复有概率空建筑的bug 学习白桦木拱门快速建筑技巧 太卡手了
        // 虚空地图 玩家死亡平台小时 更好的出题 兼容llb 地板的nbt structure增加初始偏移量
        // 增加玩家出界拉回

        //purpur 的todo 区分方块掉落和依附掉落，interact同时修改手中物品会触发两次 火把不掉落（physics event没有用)，挖掘延迟服务端控制实现（就算秒挖客户端也认为有延迟)



        // Plugin startup logic
        plugin = this;

        registerAllListeners();
        getCommand("test").setExecutor(new TestCommand());
        getCommand("end").setExecutor(new EndCommand());
        getCommand("merge").setExecutor(new MergeCommand());
        World world = Bukkit.getWorld("world");
        world.setGameRule(GameRule.DO_WEATHER_CYCLE, false);
        world.setGameRule(GameRule.DO_DAYLIGHT_CYCLE, false);
        world.setFullTime(1000);
        world.setGameRule(GameRule.RANDOM_TICK_SPEED, 0);
        world.setGameRule(GameRule.SPAWN_CHUNK_RADIUS, 0);

        ToolClass.Init();

        BlockEdit.pasteArena(GameLoop.DISPLAY_LOCATION);
        BlockEdit.pasteArena(GameLoop.CREATE_LOCATION);

        BukkitScheduler scheduler = this.getServer().getScheduler();
        scheduler.runTaskTimer(this, () -> {
            for (Player player : Bukkit.getOnlinePlayers()) {
                UUID uuid = player.getUniqueId();
                if (player.getGameMode() == GameMode.CREATIVE || player.getGameMode() == GameMode.SPECTATOR) return;
                if (GameLoop.player_data.containsKey(uuid)) {
                    PlayerData playerData = GameLoop.player_data.get(uuid);
                    int offset = playerData.getOffset();
                    Location playerBuildLocation = GameLoop.offset(GameLoop.BUILD_LOCATION, offset);
                    Location playerBlockLocation = player.getLocation().toBlockLocation();

                    int minX = playerBuildLocation.getBlockX() - 4;
                    int minY = playerBuildLocation.getBlockY() - 1;
                    int minZ = playerBuildLocation.getBlockZ() - 4;

                    int maxX = minX + 15;
                    int maxY = minY + GameLoop.STEP;
                    int maxZ = minZ + 15;

                    int x = playerBlockLocation.getBlockX();
                    int y = playerBlockLocation.getBlockY();
                    int z = playerBlockLocation.getBlockZ();
                    if (x >= minX && x <= maxX &&
                            y >= minY && y <= maxY &&
                            z >= minZ && z <= maxZ) {

                    }
                    else {
                        player.teleport(playerBuildLocation);
                    }
                }

                boolean onGround = DoubleJumpListener.isPlayerOnGroundRayTrace(player);  //站在栅栏上就爆炸了
                onGround = player.isOnGround();
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

            ToolClass.destroy_tick.entrySet().removeIf(entry -> {
                int newVal = entry.getValue() - 1;
                if (newVal <= 0) {
                    return true;          // 返回 true → 移除
                }
                entry.setValue(newVal);
                return false;
            });
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
