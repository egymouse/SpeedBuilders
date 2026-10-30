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
import org.ajls.lib.utils.ConfigU;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.structure.Mirror;
import org.bukkit.block.structure.StructureRotation;
import org.bukkit.plugin.Plugin;
import org.bukkit.structure.Structure;
import org.jetbrains.annotations.NotNull;

import java.io.File;
import java.io.FileInputStream;
import java.io.IOException;
import java.nio.file.DirectoryStream;
import java.nio.file.Files;
import java.nio.file.Path;
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

    public static List<File> getAllStructureFiles() {
//        Path dir = Paths.get("plugins/buildings");
//        if (isLLB) {
//            dir = Paths.get("plugins/buildings_llb");
//        }
        Path dir = SpeedBuilders.getPlugin().getDataFolder().toPath()
                .resolve(BUILDINGS_PATH);
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

    @NotNull
    public static List<String> getAllStructureFileNames() {


        List<File> files = getAllStructureFiles();
        List<String> fileNames = new ArrayList<>();
        for(File file : files){
            fileNames.add(file.getName());
        }
        return fileNames;

//        Path dir = SpeedBuilders.getPlugin().getDataFolder().toPath()
//                .resolve(BUILDINGS_PATH);
//        String[] names = dir.toFile().list();   // 返回文件名数组，可能为 null
//
//        if (names != null) {
//            List<String> fileNames = new ArrayList<>(Arrays.asList(names));
//            System.out.println(fileNames);
//            return fileNames;
//        }
//        return new ArrayList<>();
    }

    public static List<String> getAllStructureBuildNames() {
        List<String> buildNames = new ArrayList<>();
        List<String> fileNames = getAllStructureFileNames();
        for (String fileName : fileNames) {
            String buildName = ConfigU.getParentPath(fileName);
            buildNames.add(buildName);
        }
        return buildNames;
    }

    public static String BUILDINGS_PATH = "buildings";
    public static String BUILDINGS_LLB_PATH = "buildings_llb";
    public static String GROUNDS_PATH = "grounds";
    public static String COMMON_BUILDINGS = "common_buildings";


    public static File getStructureFile(String relativePath) {
        File file = new File(SpeedBuilders.getPlugin().getDataFolder(), relativePath);
        return file;
    }

//    public static Structure getStructure(File file) {
//        return Bukkit.getStructureManager().loadStructure(file);
//    }


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
        noBlockPhysics = true;
        structure.place(location,false, StructureRotation.NONE, Mirror.NONE,0,1.0f,new Random());
        noBlockPhysics = false;
    }
    public static boolean noBlockPhysics = false;

    public static void pasteBuildStructure(Location location, Structure structure) {
        pasteStructure(GameLoop.offset(location, 0, -1, 0), structure);
    }

    public static void fillArenaGround(Location location) {
        noBlockPhysics = true;
        int x = location.getBlockX() -4;
        int z = location.getBlockZ()-4;
        int y = location.getBlockY()-2;
        for (int dx = 0; dx < 15; dx ++) {
            for (int dz = 0; dz < 15; dz++) {
                Location locationNew = new Location(GameLoop.OVERWORLD, x+dx, y, z+dz);
                Block newBlock = locationNew.getBlock();
                newBlock.setType(Material.QUARTZ_BLOCK, false);  //llb同款
            }
        }
        noBlockPhysics = false;
    }

    public static void generateDefaultGround(Location location) {
        noBlockPhysics = true;
        int x = location.getBlockX();
        int z = location.getBlockZ();
        int y = location.getBlockY()-1;
        for (int dx = 0; dx < 7; dx ++) {
            for (int dz = 0; dz < 7; dz++) {
                Location locationNew = new Location(GameLoop.OVERWORLD, x+dx, y, z+dz);
                Block newBlock = locationNew.getBlock();
                newBlock.setType(Material.WHITE_CONCRETE, false);  //llb同款
            }
        }
        noBlockPhysics = false;
    }

//    public static void pasteStructure(Location location, Structure structure, int dx, int dy, int dz) {
//
//    }

    public static void pasteArena(Location location) {
        File file = getStructureFile(COMMON_BUILDINGS + "/" + "arena.nbt");
        Structure structure;
        try {
            structure = Bukkit.getStructureManager().loadStructure(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
//        fillArenaGround(location);
//
//        pasteBuildStructure(GameLoop.offset(location, -4, 0, -4), structure);

        pasteStructure(GameLoop.offset(location, -5, -4, -5), structure);
    }

    public static void pasteClassicArena(Location location) {
        File file = getStructureFile(COMMON_BUILDINGS + "/" + "arena_classic.nbt");
        Structure structure;
        try {
            structure = Bukkit.getStructureManager().loadStructure(file);
        } catch (IOException e) {
            throw new RuntimeException(e);
        }
        fillArenaGround(location);

        pasteBuildStructure(GameLoop.offset(location, -4, 0, -4), structure);

    }


    public static void pasteArena(int offset) {
        pasteArena(GameLoop.offset(GameLoop.BUILD_LOCATION, offset));

    }
    public static void pasteSchematic(Location location, File schematicFile) {
//        Structure structure;
//        try {
//            structure = Bukkit.getStructureManager() .loadStructure(schematicFile);
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
