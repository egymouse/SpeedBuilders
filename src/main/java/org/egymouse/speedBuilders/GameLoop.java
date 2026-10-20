package org.egymouse.speedBuilders;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.ajls.lib.utils.RandomUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.structure.Structure;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GameLoop {
    public static int timeLeft;
    public static int totalTime;
    public static GamePhase currentPhase = GamePhase.JUDGE;
    public static Set<File> leftStructureFile = null;
    public static Set<File> leftLLBStructureFile = null;
    public static HashMap<UUID, PlayerData> player_data = new HashMap<>();
    public static int DEFAULT_HEALTH = 3;
    public static int STEP = 20;
    public static String arenaString = "arena";
    public static World OVERWORLD = Bukkit.getWorld("world");
    public static int TO_CENTER = 3;
    public static int currentRound = 0;
    public static int timeForBuilding = 600;

    public static boolean killAll = false;
    public static boolean slowestDies = false;
    //todo 挖掘，板砖，掉方块，初始化arena
    //tod 楼梯拐角


    public static void start() {
        ToolClass.playing = true;
        leftStructureFile = new HashSet<>(BlockEdit.getAllSchematicFiles(false));
        leftLLBStructureFile = new HashSet<>(BlockEdit.getAllSchematicFiles(true));
        for (int i = 0; i <= ToolClass.wait_player.size(); i++) {
            BlockEdit.pasteSchematic(offset(BUILD_LOCATION, i), arenaString);



//            Structure l = Bukkit.getStructureManager().loadStructure(NamespacedKey.minecraft("labby"));
//            l.place(new Location(Bukkit.getWorld("world"),30*i,200,0),false, StructureRotation.NONE, Mirror.NONE,0,1f,new Random());
        }
        int offset = 0;
        for (Player player: ToolClass.wait_player) {
            PlayerData playerData = new PlayerData(player.getUniqueId());
            playerData.setOffset(offset);
            playerData.setHealth(DEFAULT_HEALTH);
            playerData.setPlayerName(player.getName());
            player_data.put(player.getUniqueId(), playerData);
            player.teleport(offset(BUILD_LOCATION, offset));
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.broadcastMessage(player.getName() + "进入");

            offset++;
        }
        ToolClass.wait_player.clear();


        BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();
        scheduler.runTaskLater(SpeedBuilders.getPlugin(), () -> {
            gameLoop();
        }, 100);
    }

    public static int totalBlockCount = 0;

    public static BukkitTask gameLoopTask;

    public static void gameLoop() {
        totalTime = 100;
        timeLeft = 1;
        BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();
        gameLoopTask = scheduler.runTaskTimer(SpeedBuilders.getPlugin(), () -> {
            timeLeft --;
            for (Player player : Bukkit.getOnlinePlayers()) {
                double remain = (double) timeLeft / 20.0;
//                String timeStr = String.format(java.util.Locale.ROOT, "%.1f", remain);
                String timeStr = String.format(java.util.Locale.ROOT, "%05.1f", remain);
                String phaseString = "复原";
                if (currentPhase == GamePhase.DISPLAY) {
                    phaseString = "观察";
                }
                if (currentPhase != GamePhase.JUDGE) {
                    player.spigot().sendMessage(
                            ChatMessageType.ACTION_BAR,
                            new TextComponent(ChatColor.GREEN + "剩余" + phaseString + "时间：" + ChatColor.YELLOW + timeStr)
                    );
                }
                if (timeLeft > 0) {
                    player.setLevel(Math.ceilDiv(timeLeft, 20));
                    player.setExp((float) timeLeft / totalTime);
                }
            }
            if (timeLeft <= 0) {

                int currentPhaseOrdinal = currentPhase.ordinal();
                currentPhaseOrdinal ++;
                if (currentPhaseOrdinal >= GamePhase.values().length) {
                    currentPhaseOrdinal = 0;
                }
                currentPhase = GamePhase.values()[currentPhaseOrdinal];
                Bukkit.broadcastMessage("时间到了，现在阶段" + currentPhase.name());

                if (currentPhase == GamePhase.DISPLAY) {
                    currentRound ++;
                    if (currentRound != 1) {
                        timeForBuilding -= 40;
                    }
                    generateNewBuild();
                    BlockEdit.pasteStructure(DISPLAY_LOCATION, currentBuildStructure, currentIsLLB);
                    totalBlockCount = 0;
                    Inventory inventory = Bukkit.createInventory(null, 36);
                    int x = DISPLAY_LOCATION.getBlockX();
                    int y = DISPLAY_LOCATION.getBlockY();
                    int z = DISPLAY_LOCATION.getBlockZ();
                    for (int dy = 0; dy < 7; dy ++) {
                        for (int dx = 0; dx < 7; dx ++) {
                            for (int dz = 0; dz < 7; dz ++) {
                                Location location = new Location(OVERWORLD, x+dx, y+dy, z+dz);
                                Block block = location.getBlock();
                                Material material = block.getType();
                                // 跳过空气
                                if (material == Material.AIR) continue;
                                totalBlockCount++;

                                BlockData blockData = block.getBlockData();
                                // 如果是 Bisected 方块（门、床等），只处理下半部分
                                if (blockData instanceof Bisected) {
                                    Bisected bisected = (Bisected) blockData;
                                    if (bisected.getHalf() != Bisected.Half.BOTTOM) {
                                        continue; // 跳过上半部分，避免重复
                                    }
                                }
                                ItemStack itemStack = new ItemStack(material);
                                inventory.addItem(itemStack);

                            }
                        }
                    }


                    for (PlayerData playerData : player_data.values()) {
                        playerData.setAllCorrect(false);


                        int offset = playerData.getOffset();
                        UUID uuid = playerData.getUuid();
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null) continue;
                        Location location = offset(BUILD_LOCATION, offset);
                        player.teleport(location);
                        player.setGameMode(GameMode.SURVIVAL);
                        BlockEdit.pasteStructure(location, currentBuildStructure, currentIsLLB);
                        Inventory playerInventory = player.getInventory();
                        for (int i = 0; i <36; i++) {
                            ItemStack itemStack = inventory.getItem(i);
                            playerInventory.setItem(i, itemStack);
                        }
                    }

                }
                else if (currentPhase == GamePhase.BUILD) {
                    displayFill(Material.BEDROCK);
                    for (PlayerData playerData : player_data.values()) {
                        int offset = playerData.getOffset();
                        Location buildLocation = offset(BUILD_LOCATION, offset);
                        {
                            int x = buildLocation.getBlockX();
                            int y = buildLocation.getBlockY();
                            int z = buildLocation.getBlockZ();
                            for (int dy = 0; dy < 7; dy ++) {
                                for (int dx = 0; dx < 7; dx ++) {
                                    for (int dz = 0; dz < 7; dz ++) {
                                        Location locationNew = new Location(OVERWORLD, x+dx, y+dy, z+dz);
                                        locationNew.getBlock().setType(Material.AIR, false);
                                    }
                                }
                            }
                        }
                        playerFill(buildLocation, Material.BEDROCK);


                    }
                }
                else if (currentPhase == GamePhase.JUDGE) {
                    for (PlayerData playerData : player_data.values()) {
                        int offset = playerData.getOffset();
                        Location buildLocation = offset(BUILD_LOCATION, offset);
                        UUID uuid = playerData.getUuid();
                        Player player = Bukkit.getPlayer(uuid);
                        if (player != null) {
                            player.setGameMode(GameMode.SPECTATOR);
                        }
                        playerFill(buildLocation, Material.AIR);
                        if (playerData.isAllCorrect()) continue;
                        int correctBlocks = 0;
                        int misplacedBlocks = 0;
                        {
                            int x = buildLocation.getBlockX();
                            int y = buildLocation.getBlockY();
                            int z = buildLocation.getBlockZ();
                            for (int dy = 0; dy < 7; dy ++) {
                                for (int dx = 0; dx < 7; dx ++) {
                                    for (int dz = 0; dz < 7; dz ++) {
                                        Location worldLocation = new Location(OVERWORLD, x+dx, y+dy, z+dz);
                                        Block worldBlock = worldLocation.getBlock();
                                        if (worldBlock.getType() == Material.AIR) continue;
                                        Location displayLocation = offset(DISPLAY_LOCATION, dx, dy, dz);
                                        Block displayBlock = displayLocation.getBlock();

                                        if (isBlockSame(displayBlock, worldBlock)) {
                                            correctBlocks++;
                                        }
                                        else {
                                            misplacedBlocks++;
                                        }
                                    }
                                }
                            }
                            playerData.setBlockRight(correctBlocks);
                            playerData.setBlockWrong(misplacedBlocks);
                        }

                    }
                    displayFill(Material.AIR);
//                    BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();

                    scheduler.runTaskLater(SpeedBuilders.getPlugin(), () -> {
                        List<PlayerData> playerDataList = new ArrayList<PlayerData>(player_data.values());  //修复了内存引用直接把Playerdata删除的Bug
                        List<PlayerData> sortedPlayerDataList = new ArrayList<>();
                        Bukkit.broadcastMessage(totalBlockCount + "总方块数");


                        HashSet<PlayerData> allCorrectPlayerData = new HashSet<>();
                        for (PlayerData playerData : playerDataList) {
                            if (playerData.isAllCorrect()) {
                                Bukkit.broadcastMessage(playerData.getPlayerName() + ":全对" + " 生命:" + playerData.getHealth());
                                allCorrectPlayerData.add(playerData);

                            }
                        }

                        for (PlayerData playerData : allCorrectPlayerData) {
                            playerDataList.remove(playerData);
                        }


                        while (!playerDataList.isEmpty()) {
                            PlayerData bestPlayerData = null;
                            for (PlayerData playerData : playerDataList) {

                                if (bestPlayerData == null) {
                                    bestPlayerData = playerData;
                                }
                                else {
                                    if (playerData.getBlockRight() > bestPlayerData.getBlockRight()) {
                                        bestPlayerData = playerData;
                                    }
                                }

                            }
                            if (bestPlayerData != null) {
                                sortedPlayerDataList.add(bestPlayerData);
                                playerDataList.remove(bestPlayerData);
                            }
                        }

                        for (int i = 0; i < sortedPlayerDataList.size(); i ++) {
                            PlayerData playerData = sortedPlayerDataList.get(i);

                            UUID uuid = playerData.getUuid();
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                Bukkit.broadcastMessage(player.getName() + ":" + playerData.getBlockRight() + "对的方块数" + " 生命:" + playerData.getHealth());
                            }
                        }
                        if (sortedPlayerDataList.isEmpty()) {
                            Bukkit.broadcastMessage("很棒嘛 是平安夜");
                        }
                        else {

                            PlayerData lastPlayerData = sortedPlayerDataList.getLast();
                            lastPlayerData.setHealth(lastPlayerData.getHealth()-1);
                            Bukkit.broadcastMessage(lastPlayerData.getPlayerName() + "的生命变成了" + lastPlayerData.getHealth());

                            if (lastPlayerData.getHealth() <= 0) {
                                Bukkit.broadcastMessage(lastPlayerData.getPlayerName() + "飘零了");
                                player_data.remove(lastPlayerData.getUuid());
                                Bukkit.broadcastMessage(player_data.size() + " remain");
                                if (player_data.size() == 1) {
                                    for (UUID uuid : player_data.keySet()) {
                                        Player player = Bukkit.getPlayer(uuid);
                                        if (player != null)  {
                                            String name = player.getName();
                                            Bukkit.broadcastMessage(name + "赢了");
                                            if (killAll) {
                                                Bukkit.broadcastMessage("但是你以为赢了就能走吗?");
                                            }
                                            else {
                                                end();
                                            }
                                        }
                                    }
                                }
                                else if (player_data.size() == 0) {
                                    Bukkit.broadcastMessage("无人生还");
                                    end();
                                }
                            }
                        }





                    }, 50);




                }

                if (currentPhase != GamePhase.BUILD) {
                    if (currentPhase == GamePhase.DISPLAY) {
                        timeLeft = 15*20;
                    }
                    else {
                        timeLeft = 100;
                    }

                }
                else {
                    timeLeft = 1200 - currentRound*40 ; //todo 逐渐减少
                }
                totalTime = timeLeft;



            }
            else {
                if (currentPhase == GamePhase.BUILD) {
                    boolean existSomeoneUnfinished = false;
                    int unfinishedCount = 0;
                    PlayerData slowestData = null;
                    for (PlayerData playerData : player_data.values()) {
                        if (playerData.isAllCorrect()) continue;
                        if (allCorrect(playerData)) {
                            playerData.setAllCorrect(true);
                            playerData.setCompletionTicks(totalTime - timeLeft);
                            Bukkit.broadcastMessage(ChatColor.GREEN + playerData.getPlayerName() + ChatColor.WHITE + "在" + ChatColor.YELLOW + (float) playerData.getCompletionTicks() / 20 + "s" + ChatColor.WHITE + "内完成");
                            UUID uuid = playerData.getUuid();
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                player.setGameMode(GameMode.SPECTATOR);
                            }
                        }
                        else {
                            existSomeoneUnfinished = true;
                            unfinishedCount ++;
                            slowestData = playerData;
                        }


                    }
                    if (!existSomeoneUnfinished) {
                        timeLeft = 1;
                        Bukkit.broadcastMessage("全部完成");
                    }
                    else {
                        if (slowestDies && !killAll) {
                            if (unfinishedCount == 1) {
                                Bukkit.broadcastMessage(slowestData.getPlayerName() + "太慢了");
                                Bukkit.broadcastMessage("其实插件有bug，1tick内你能完成你就能活下来");
                            }
                        }
                    }
                }
            }
        }, 1, 1);

    }

    public static void end() {
        gameLoopTask.cancel();
        ToolClass.playing = false;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                player.teleport(offset(BUILD_LOCATION, 0, 0, -1));
                player.setGameMode(GameMode.SURVIVAL);
                player.getInventory().clear();
                player.getInventory().setItem(0, ToolClass.create);
                player.getInventory().setItem(1, ToolClass.play);
            }
        }
    }


    public static boolean allCorrect(PlayerData playerData) {
        int offset = playerData.getOffset();
        Location buildLocation = offset(BUILD_LOCATION, offset);
        UUID uuid = playerData.getUuid();
        Player player = Bukkit.getPlayer(uuid);
        {
            int x = buildLocation.getBlockX();
            int y = buildLocation.getBlockY();
            int z = buildLocation.getBlockZ();
            for (int dy = 0; dy < 7; dy++) {
                for (int dx = 0; dx < 7; dx++) {
                    for (int dz = 0; dz < 7; dz++) {
                        Location locationDisplay = offset(DISPLAY_LOCATION, dx, dy, dz);
                        Block displayBlock = locationDisplay.getBlock();
                        if (displayBlock.getType() == Material.AIR) continue;

                        Location worldLocation = new Location(OVERWORLD, x + dx, y + dy, z + dz);
                        Block worldBlock = worldLocation.getBlock();

                        if (!isBlockSame(displayBlock, worldBlock)) return false;

                    }
                }
            }
        }
        return true;
    }

    public static boolean isBlockSame(Block displayBlock, Block worldBlock) {
        if(displayBlock.getType() != worldBlock.getType()){
            return false;
        }
        if(displayBlock.getBlockData() instanceof Stairs){
            if(!StairsVisualComparator.Compare(displayBlock.getBlockData(),worldBlock.getBlockData())){
                return false;
            }
        }
        else{
            if(!worldBlock.getBlockData().matches(displayBlock.getBlockData())){
                return false;
            }
        }
        return true;
    }

    public static void displayFill(Material material) {
        {
            int x = DISPLAY_LOCATION.getBlockX() + TO_CENTER - STEP/2;
            int y = DISPLAY_LOCATION.getBlockY();
            int z = DISPLAY_LOCATION.getBlockZ() + TO_CENTER - STEP/2;
            for (int dy = 0; dy <= STEP; dy ++) {
                for (int dx = 0; dx <= STEP; dx ++) {
                    Location locationNew = new Location(OVERWORLD, x+dx, y+dy, z);
                    locationNew.getBlock().setType(material, false);
                }
            }
            playerFill(DISPLAY_LOCATION, material);
        }
    }

    public static void playerFill(Location buildLocation, Material material) {
        {
            int x = buildLocation.getBlockX() + TO_CENTER - STEP/2;
            int y = buildLocation.getBlockY();
            int z = buildLocation.getBlockZ() + TO_CENTER - STEP/2;
            for (int dy = 0; dy <= STEP; dy ++) {
                for (int dz = 0; dz <= STEP; dz ++) {
                    Location locationNew = new Location(OVERWORLD, x, y+dy, z+dz);
                    locationNew.getBlock().setType(material, false);
                }
            }
        }
        {
            int x = buildLocation.getBlockX() + TO_CENTER + STEP/2;
            int y = buildLocation.getBlockY();
            int z = buildLocation.getBlockZ() + TO_CENTER - STEP/2;
            for (int dy = 0; dy <= STEP; dy ++) {
                for (int dz = 0; dz <= STEP; dz ++) {
                    Location locationNew = new Location(OVERWORLD, x, y+dy, z+dz);
                    locationNew.getBlock().setType(material, false);
                }
            }
        }
    }

    public static Location offset(Location location, int offset) {
        return location.clone().offset(STEP*offset, 0, 0).toLocation(location.getWorld());
    }

    public static Location offset(Location location, int x, int y, int z) {
        return location.clone().offset(x, y, z).toLocation(location.getWorld());
    }

    public static File currentBuildFile;
    public static Structure currentBuildStructure;
    public static boolean currentIsLLB;

    public static void generateNewBuild() {
        if (leftStructureFile.isEmpty() && leftLLBStructureFile.isEmpty()) {
            leftStructureFile = new HashSet<>(BlockEdit.getAllSchematicFiles(false));
            leftLLBStructureFile = new HashSet<>(BlockEdit.getAllSchematicFiles(true));
        }


        List<File> leftStructureFileList = new ArrayList<File>(leftStructureFile);
        List<File> leftLLBStructureFileList = new ArrayList<File>(leftLLBStructureFile);
        File file;
        int random = RandomUtils.random(0, leftLLBStructureFileList.size() + leftStructureFileList.size() - 1);
        if (random >= leftStructureFileList.size()) {
            random -= leftStructureFileList.size();
            currentIsLLB = true;
            file = leftLLBStructureFileList.get(random);
        }
        else {
            currentIsLLB = false;
            file = leftStructureFileList.get(random);
        }
        currentBuildFile = file;
        leftStructureFile.remove(file);

        try {
            currentBuildStructure = Bukkit.getStructureManager().loadStructure(file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Location BUILD_LOCATION = new Location(Bukkit.getWorld("world"), -3, -60, -3);
    public static Location DISPLAY_LOCATION = offset(BUILD_LOCATION, 0, 0, 20);
    public static Location CREATE_LOCATION = offset(BUILD_LOCATION, 0, 0, -20);



    public enum GamePhase {
        DISPLAY,
        BUILD,
        JUDGE,
    }
}
