package org.egymouse.speedBuilders.commands;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.block.Block;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.structure.Structure;
import org.bukkit.structure.StructureManager;
import org.bukkit.util.BlockVector;
import org.egymouse.speedBuilders.BlockEdit;
import org.egymouse.speedBuilders.GameLoop;
import org.egymouse.speedBuilders.SpeedBuilders;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.List;

public class MergeCommand implements CommandExecutor {
    @Override
    public boolean onCommand(@NotNull CommandSender sender, @NotNull Command command, @NotNull String label, @NotNull String[] args) {


        Path pluginPath = SpeedBuilders.getPlugin().getDataFolder().toPath();
        Path buildingPath = pluginPath.resolve(BlockEdit.BUILDINGS_LLB_PATH);
        Path groundPath = pluginPath.resolve(BlockEdit.GROUNDS_PATH);
        Path myBuildingPath = pluginPath.resolve(BlockEdit.BUILDINGS_PATH);
        StructureManager structureManager = Bukkit.getStructureManager();


        try(DirectoryStream<Path> stream = Files.newDirectoryStream(buildingPath,"*.nbt")){
            for(Path entry : stream){
                if(Files.isRegularFile(entry)){
                    File buildStructureFile = entry.toFile();
                    String fileName = buildStructureFile.getName();
                    File myBuildFile = myBuildingPath.resolve(fileName).toFile();
                    if (myBuildFile.exists()) continue;
                    File groundFile = groundPath.resolve(fileName).toFile();
                    Structure buildStructure;
                    Structure groundStructure;
                    Structure mergedStructure = structureManager.createStructure();
                    try {
                        buildStructure = structureManager.loadStructure(buildStructureFile);
                    } catch (IOException e) {
                        throw new RuntimeException(e);
                    }
                    if (groundFile.exists()) {
                        try {
                            groundStructure = structureManager.loadStructure(groundFile);
                        } catch (IOException e) {
                            throw new RuntimeException(e);
                        }
                        BlockEdit.pasteBuildStructure(GameLoop.STRUCTURE_CACHE_LOCATION, groundStructure);
                    }
                    else {
                        BlockEdit.generateDefaultGround(GameLoop.STRUCTURE_CACHE_LOCATION);
                    }
                    BlockEdit.pasteStructure(GameLoop.STRUCTURE_CACHE_LOCATION, buildStructure);

                    mergedStructure.fill(GameLoop.offset(GameLoop.STRUCTURE_CACHE_LOCATION, 0, -1, 0),new BlockVector(7,8,7),false);
                    try {
                        // 创建完整的父目录（如果父目录已存在则静默跳过）
                        Files.createDirectories(myBuildFile.getParentFile().toPath());
                        Bukkit.getStructureManager().saveStructure(myBuildFile,mergedStructure);
                    } catch (IOException ex) {
                        sender.sendMessage(ex.toString());
                        return false;
                    }

//                    File groundFile = new File(groundPath.toFile(), fileName);
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }

        return true;
    }
}
