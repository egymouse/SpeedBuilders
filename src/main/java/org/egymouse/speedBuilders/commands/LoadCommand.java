package org.egymouse.speedBuilders.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.structure.Structure;
import org.egymouse.speedBuilders.BlockEdit;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.SpeedBuilders;
import org.egymouse.speedBuilders.TrainManager;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.util.UUID;

public class LoadCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
        CommandSender player = sender;

        if (args.length == 0) {
            sender.sendMessage("/load 题目名字");
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
            sender.sendMessage(ChatColor.RED + "貌似这个题目还不存在");
            return true;
        }

        Structure structure = null;
        try {
            structure = Bukkit.getStructureManager().loadStructure(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }

        BlockEdit.pasteBuildStructure(GameLoop.CREATE_LOCATION, structure);
        sender.sendMessage(ChatColor.YELLOW + "已放置" + buildName);




        return true;
    }
}
