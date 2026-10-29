package org.egymouse.speedBuilders.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.egymouse.speedBuilders.*;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.UUID;

public class TrainCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        GameLoop.generateNewBuild();
//        BlockEdit.pasteSchematic(GameLoop.BUILD_LOCATION, GameLoop.currentBuildFile);
//        return true;
        if ( !(sender instanceof Player) ) {
            sender.sendMessage("你是玩家吗你就训练");
            return true;
        }
        Player player = (Player) sender;
        UUID uuid = player.getUniqueId();

        if (args.length == 0) {
            if (TrainManager.player_trainData.containsKey(uuid)) {
                TrainManager.startTrain(player);
            }
            else {
                player.sendMessage(ChatColor.RED + "/train 题目名字");
            }
            return true;
        }


        StringBuilder stringBuilder = new StringBuilder();

        for (int i = 0; i < args.length; i++){
            stringBuilder.append(args[i]);
            if (i != args.length-1){
                stringBuilder.append(" ");
            }
        }
        String buildName = stringBuilder.toString();


        File file = new File(SpeedBuilders.getPlugin().getDataFolder(), BlockEdit.BUILDINGS_PATH + "/" + buildName + ".nbt");
        if (!file.exists()) {
            player.sendMessage(ChatColor.RED + "貌似这个题目还不存在");
            return true;
        }

        Structure structure = null;
        try {
            structure = Bukkit.getStructureManager().loadStructure(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        TrainManager.tryRegisterPlayer(player, structure, buildName);




        return true;
    }
}
