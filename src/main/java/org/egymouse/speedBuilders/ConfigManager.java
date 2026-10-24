package org.egymouse.speedBuilders;

import org.bukkit.configuration.Configuration;

import java.util.UUID;

public class ConfigManager {
    public static String getPersonalBestTimePath(UUID uuid, String buildName) {
        return uuid + "." + "personal_best_time" + "." + buildName;
    }
    public static int getPersonalBestTime(UUID uuid, String buildName) {
        Configuration configuration = SpeedBuilders.getPlugin().getConfig();
        int pb = configuration.getInt(getPersonalBestTimePath(uuid, buildName));
        return pb;
    }

    public static void setPersonalBestTIme(UUID uuid, String buildName, int timeInTicks) {
        Configuration configuration = SpeedBuilders.getPlugin().getConfig();
        configuration.set(getPersonalBestTimePath(uuid, buildName), timeInTicks);
        SpeedBuilders.getPlugin().saveConfig();
    }

    public static boolean tryUpdatePersonalBestTime(UUID uuid, String buildName, int timeInTicks) {
        int pb = getPersonalBestTime(uuid, buildName);
        if (timeInTicks < pb || pb == 0) {
            setPersonalBestTIme(uuid, buildName, timeInTicks);
            return true;
        }
        return false;
    }

}
