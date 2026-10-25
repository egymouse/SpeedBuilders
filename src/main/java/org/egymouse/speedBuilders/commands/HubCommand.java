package org.egymouse.speedBuilders.commands;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.TrainManager;
import org.egymouse.speedBuilders.TrainPlayerData;
import org.egymouse.speedBuilders.listeners.PlayerJoinEventListener;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class HubCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {

        if (sender instanceof Player player) {
            UUID uuid = player.getUniqueId();
            if (TrainManager.player_trainData.containsKey(uuid)) {
                TrainManager.tryUnregisterPlayer(player);
            }
            else {
//                PlayerJoinEventListener.resetPlayer(player);  //还没和主游戏循环对接呢
            }

        }

        return true;
    }
}
