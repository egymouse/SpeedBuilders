package org.egymouse.speedBuilders.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.egymouse.speedBuilders.BlockEdit;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.SpeedBuilders;
import org.egymouse.speedBuilders.ToolClass;
import org.jspecify.annotations.NonNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;

public class EndCommand implements CommandExecutor {

    @Override
    public boolean onCommand(@NonNull CommandSender sender, @NonNull Command command, @NonNull String label, @NonNull String[] args) {
        if (args.length == 0){
            sender.sendMessage(ChatColor.RED + "/end 题目名字");
            return true;
        }
        String name = args[0];

        Structure structure = Bukkit.getStructureManager().createStructure();

        structure.fill(GameLoop.offset(GameLoop.CREATE_LOCATION, 0, -1, 0),new BlockVector(7,8,7),false);
//        Path dir = Paths.get("plugins/buildings/" + name + ".nbt");
//        File file = dir.toFile();
        File file = new File(SpeedBuilders.getPlugin().getDataFolder(), BlockEdit.BUILDINGS_PATH + "/" + name + ".nbt");
        if(!file.exists()){
            try {
                // 创建完整的父目录（如果父目录已存在则静默跳过）
                Files.createDirectories(file.getParentFile().toPath());
                Bukkit.getStructureManager().saveStructure(file,structure);
            } catch (IOException ex) {
                sender.sendMessage(ex.toString());
                return false;
            }
            sender.sendMessage(ChatColor.YELLOW + "保存了 " + name);
        }else{
            sender.sendMessage(ChatColor.RED + name  + " 已存在，换一个名字试试吧");
        }

        return false;
    }
}
