package org.egymouse.speedBuilders;
import com.sk89q.worldedit.EditSession;
import com.sk89q.worldedit.WorldEdit;
import com.sk89q.worldedit.WorldEditException;
import com.sk89q.worldedit.bukkit.BukkitAdapter;
import com.sk89q.worldedit.extent.clipboard.Clipboard;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormat;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardFormats;
import com.sk89q.worldedit.extent.clipboard.io.ClipboardReader;
import com.sk89q.worldedit.function.operation.Operation;
import com.sk89q.worldedit.function.operation.Operations;
import com.sk89q.worldedit.math.BlockVector3;
import com.sk89q.worldedit.session.ClipboardHolder;
import com.sk89q.worldedit.world.World;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.plugin.Plugin;
import org.bukkit.structure.Structure;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
import java.nio.file.Paths;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.List;
import java.util.Random;

public class BlockEdit {





    public static void pasteSchematic(Location location, String fileName) {


        File worldEditDataFolder = getWorldEditDataFolder();
        File file = new File(worldEditDataFolder, "schematics/" + fileName);
        pasteSchematic(location, file);



    }

    public static List<File> getAllSchematicFiles() {
//        File dir = getWorldEditDataFolder();
//        File schemsDir = new File(dir, "schematics/");
//        String[] names = schemsDir.list();   // 返回文件名数组，可能为 null
//
//        if (names != null) {
//            List<String> fileNames = new ArrayList<>(Arrays.asList(names));
//            System.out.println(fileNames);
//            return fileNames;
//        }
//        return null;

        Path dir = Paths.get("plugins/buildings");
        List<File> all_structures = new ArrayList<>();
        try(DirectoryStream<Path> stream = Files.newDirectoryStream(dir,"*.nbt")){
            for(Path entry : stream){
                if(Files.isRegularFile(entry)){
                    all_structures.add(entry.toFile());
                }
            }
        }catch (IOException e){
            e.printStackTrace();
        }
        return all_structures;
    }

    public static File getWorldEditDataFolder() {
        // 获取 WorldEdit 插件实例
        Plugin worldEditPlugin = Bukkit.getPluginManager().getPlugin("WorldEdit");

// 重要：先检查插件是否已加载，避免 NullPointerException
        if (worldEditPlugin != null && worldEditPlugin.isEnabled()) {
            // 获取 WorldEdit 的数据文件夹 (通常是 plugins/WorldEdit)
            File dataFolder = worldEditPlugin.getDataFolder();
            // 现在你可以操作这个 File 对象，例如读取其中的 schematic 文件
            return dataFolder;

        } else {
            // WorldEdit 未安装或未启用，进行相应的错误处理
            Bukkit.broadcastMessage("worldedit呢");
        }
        return null;

    }





    public static void pasteStructure(Location location, Structure structure) {
        location = GameLoop.offset(location, 0, -1, 0);
        structure.place(location,false, StructureRotation.NONE, Mirror.NONE,0,1.0f,new Random());
    }
    public static void pasteSchematic(Location location, File schematicFile) {
//        Structure structure;
//        try {
//            structure = Bukkit.getStructureManager().loadStructure(schematicFile);
//        }
//        catch (IOException e) {
//            throw new RuntimeException(e);
//        }
//
//
//        structure.place(location,false, StructureRotation.NONE, Mirror.NONE,0,1.0f,new Random());

        // 1. 检测文件格式并创建读取器
        ClipboardFormat format = ClipboardFormats.findByFile(schematicFile);
        if (format == null) {
            // 处理无法识别格式的情况
            return;
        }

        Clipboard clipboard;
        try (ClipboardReader reader = format.getReader(new FileInputStream(schematicFile))) {
            // 2. 读取文件到 Clipboard 对象
            clipboard = reader.read();
        } catch (IOException e) {
            e.printStackTrace();
            return;
        }

        // 3. 将 Bukkit 世界适配为 WorldEdit 的 World
        World world = BukkitAdapter.adapt(location.getWorld());
        BlockVector3 pastePosition = BlockVector3.at(location.getX(), location.getY(), location.getZ());

        // 4. 创建一个 EditSession 来执行编辑操作
        try (EditSession editSession = WorldEdit.getInstance().getEditSessionFactory()
                .getEditSession(world, -1)) { // -1 表示不限制方块修改数量

            // 5. 使用 ClipboardHolder 构建粘贴操作
            Operation operation = new ClipboardHolder(clipboard)
                    .createPaste(editSession)
                    .to(pastePosition)
                    .ignoreAirBlocks(false) // 设为 true 可跳过空气
                    .build();

            // 6. 执行操作
            Operations.complete(operation);
        } catch (WorldEditException e) {
            throw new RuntimeException(e);
        }


    }
}
