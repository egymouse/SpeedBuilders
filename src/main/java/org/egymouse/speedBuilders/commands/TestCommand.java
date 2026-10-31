package org.egymouse.speedBuilders.commands;

import org.ajls.lib.utils.ConfigU;
import org.ajls.lib.utils.RandomUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.structure.Structure;
import org.bukkit.util.BlockVector;
import org.egymouse.speedBuilders.BlockEdit;
import org.egymouse.speedBuilders.GameLoop;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.HashSet;
import java.util.List;

public class TestCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {
//        GameLoop.generateNewBuild();
//        BlockEdit.pasteSchematic(GameLoop.BUILD_LOCATION, GameLoop.currentBuildFile);
//        return true;





        List<File> leftStructureFileList = BlockEdit.getAllStructureFiles();
        for (int i = 0 ; i < 11; i++) {
            for (int j = 0; j < 11; j++) {
                int index = i*11+j;
                File file = leftStructureFileList.get(index);
                Structure structure;
                try {
                    structure = Bukkit.getStructureManager().loadStructure(file);
                }
                catch (IOException e) {
                    throw new RuntimeException(e);
                }
                Location location = GameLoop.offset(new Location(GameLoop.OVERWORLD, 0, -60, 0), j*8, 0, i*8);
                BlockEdit.fillSmallQuartzGround(location);
                BlockEdit.pasteBuildStructure(location, structure);
            }
        }
        return true;

//        for (File file : leftStructureFileList) {
//
//        }


        /*
        if (true) return true;

        Structure structure = Bukkit.getStructureManager().createStructure();

        structure.fill(new Location(GameLoop.OVERWORLD, 17, 146, -9), new BlockVector(17,4,17),false);
//        structure.fill(new Location(GameLoop.OVERWORLD, -7, -61, -7), new BlockVector(15,1,15),false);
        Path dir = Paths.get("plugins/buildings/" + "bjd_arena" + ".nbt");
        File file = dir.toFile();
        if(!file.exists()){
            try {
                // 创建完整的父目录（如果父目录已存在则静默跳过）
                Files.createDirectories(file.getParentFile().toPath());
                Bukkit.getStructureManager().saveStructure(file,structure);
            } catch (IOException ex) {
                sender.sendMessage(ex.toString());
                return false;
            }
        }else{
        }

        return true;

         */
    }
}
