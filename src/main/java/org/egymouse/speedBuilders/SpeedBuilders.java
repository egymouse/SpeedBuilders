package org.egymouse.speedBuilders;

import io.github.classgraph.ClassGraph;
import io.github.classgraph.ClassInfoList;
import io.github.classgraph.ScanResult;
import org.bukkit.event.Listener;
import org.bukkit.plugin.java.JavaPlugin;
import org.egymouse.speedBuilders.listeners.InstantBreakListener;

public final class SpeedBuilders extends JavaPlugin {

    @Override
    public void onEnable() {
        // Plugin startup logic

        registerAllListeners();


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

    @Override
    public void onDisable() {
        // Plugin shutdown logic
    }
}
