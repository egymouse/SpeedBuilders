package org.egymouse.speedBuilders;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
import org.ajls.lib.utils.ConfigU;
import org.ajls.lib.utils.RandomUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.BlockState;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.type.Bed;
import org.bukkit.block.data.type.SmallDripleaf;
import org.bukkit.block.data.type.Stairs;
import org.bukkit.block.data.type.TrapDoor;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.structure.Structure;
import org.egymouse.speedBuilders.listeners.PlayerJoinEventListener;

import java.io.File;
import java.io.IOException;
import java.util.*;

public class GameLoop {
    public static int timeLeft;
    public static int totalTime;
    public static GamePhase currentPhase = GamePhase.JUDGE;
    public static Set<File> leftStructureFile = null;
    public static HashMap<UUID, PlayerData> player_data = new HashMap<>();
    public static int DEFAULT_HEALTH = 3;
    public static int STEP = 20;
    public static String arenaString = "arena.schem";
    public static World OVERWORLD = Bukkit.getWorld("world");
    public static int TO_CENTER = 3;
    public static int currentRound = 0;
    public static int timeForBuilding = 600;

    public static boolean killAll = false;
    public static boolean slowestDies = false;
    public static boolean noPeeking = false;
    public static boolean allowBreak = false;
    //todo 掉方块


    public static void start() {
        ToolClass.playing = true;
        leftStructureFile = new HashSet<>(BlockEdit.getAllStructureFiles());
        currentRound = 0;
        currentPhase = GamePhase.JUDGE;
        for (int i = 0; i < ToolClass.wait_player.size(); i++) {
            BlockEdit.pasteArena(i);
        }
        int offset = 0;
        for (Player player: ToolClass.wait_player) {
            PlayerData playerData = new PlayerData(player.getUniqueId());
            playerData.setOffset(offset);
            playerData.setHealth(DEFAULT_HEALTH);
            playerData.setPlayerName(player.getName());
            player_data.put(player.getUniqueId(), playerData);
            player.teleport(
                    toTeleportLocation(
                            offset(
                                    offset(BUILD_LOCATION, offset), 0, 0, -1)));
            player.setGameMode(GameMode.SURVIVAL);
            Bukkit.broadcastMessage(player.getName() + "进入");

            offset++;
        }
        ToolClass.wait_player.clear();


        BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();
        scheduler.runTaskLater(SpeedBuilders.getPlugin(), () -> {
            gameLoop();
        }, 60);
    }

    public static int totalBlockCount = 0;

    public static BukkitTask gameLoopTask;

    public static int DISPLAY_TIME = 300; //15*20 = 300
    public static int BUILD_TIME = 12000;
    public static int JUDGE_TIME = 100;

    public static void gameLoop() {
        totalTime = 100;
        timeLeft = 1;
        BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();
        gameLoopTask = scheduler.runTaskTimer(SpeedBuilders.getPlugin(), () -> {
            timeLeft --;
            for (Player player : Bukkit.getOnlinePlayers()) {
                double remain = (double) timeLeft / 20.0;
//                String timeStr = String.format(java.util.Locale.ROOT, "%.1f", remain);
                String timeStr = String.format(java.util.Locale.ROOT, "%05.2f", remain);
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
//                Bukkit.broadcastMessage("时间到了，现在阶段" + currentPhase.name());

                if (currentPhase == GamePhase.DISPLAY) {

                    currentRound ++;
                    if (currentRound != 1) {
                        timeForBuilding -= 40;
                    }
                    generateNewBuild();
                    Bukkit.broadcastMessage(ChatColor.AQUA + "建筑名:" + currentBuildName);

                    BlockEdit.pasteBuildStructure(DISPLAY_LOCATION, currentBuildStructure);
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

                                if(blockData instanceof Bed bed){
                                    if(bed.getPart() != Bed.Part.HEAD){
                                        continue;
                                    }

                                }else if(blockData instanceof Bisected b){
                                    if(blockData instanceof Stairs || blockData instanceof TrapDoor || blockData instanceof SmallDripleaf){
//                                        ToolClass.playing_player.get(i).getInventory().addItem(new ItemStack(state.getType()));
                                    }else if(b.getHalf() != Bisected.Half.BOTTOM){
                                        continue;
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
                        player.teleport(toTeleportLocation(offset(location, 0, 0, -1)));
                        player.setGameMode(GameMode.SURVIVAL);

//                        displayStructure(playerData);

                        BlockEdit.pasteBuildStructure(location, currentBuildStructure);
                        Inventory playerInventory = player.getInventory();
                        for (int i = 0; i <36; i++) {
                            ItemStack itemStack = inventory.getItem(i);
                            playerInventory.setItem(i, itemStack);
                        }
                    }

                }
                else if (currentPhase == GamePhase.BUILD) {
//                    displayFill(Material.BEDROCK);

                    fillAir(DISPLAY_LOCATION);
                    for (PlayerData playerData : player_data.values()) {
                        int offset = playerData.getOffset();
                        Location buildLocation = offset(BUILD_LOCATION, offset);
                        fillAir(buildLocation);
                        if (noPeeking) {
                            playerFill(buildLocation, Material.BEDROCK);
                        }

                    }
                }
                else if (currentPhase == GamePhase.JUDGE) {
                    BlockEdit.pasteBuildStructure(DISPLAY_LOCATION, currentBuildStructure);

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
//                    displayFill(Material.AIR);

                    scheduler.runTaskLater(SpeedBuilders.getPlugin(), () -> {
                        List<PlayerData> playerDataList = new ArrayList<PlayerData>(player_data.values());  //修复了内存引用直接把Playerdata删除的Bug
                        List<PlayerData> sortedPlayerDataList = new ArrayList<>();
                        Bukkit.broadcastMessage(ChatColor.LIGHT_PURPLE + "总方块数:" + totalBlockCount);


                        HashSet<PlayerData> allCorrectPlayerData = new HashSet<>();
                        for (PlayerData playerData : playerDataList) {
                            if (playerData.isAllCorrect()) {
                                Bukkit.broadcastMessage(ChatColor.DARK_GREEN + playerData.getPlayerName() + ":全对" + " 生命:" + playerData.getHealth());
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
                                Bukkit.broadcastMessage(ChatColor.GREEN + player.getName() + ":" + playerData.getBlockRight() + "对" + " 生命:" + playerData.getHealth());
                            }
                        }
                        if (sortedPlayerDataList.isEmpty()) {
                            Bukkit.broadcastMessage("很棒嘛 是平安夜");
                        }
                        else {

                            PlayerData lastPlayerData = sortedPlayerDataList.getLast();
                            lastPlayerData.setHealth(lastPlayerData.getHealth()-1);
                            Bukkit.broadcastMessage(ChatColor.YELLOW + lastPlayerData.getPlayerName() + "的生命变成了" + lastPlayerData.getHealth());

                            if (lastPlayerData.getHealth() <= 0) {
                                Bukkit.broadcastMessage(ChatColor.RED + lastPlayerData.getPlayerName() + "飘零了");
                                player_data.remove(lastPlayerData.getUuid());
                                destroyPlatform(lastPlayerData.getBuildLocation());
                                Bukkit.broadcastMessage(ChatColor.BLUE + "还剩" + player_data.size() + "名玩家");
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





                    }, JUDGE_TIME/2);




                }

                if (currentPhase != GamePhase.BUILD) {
                    if (currentPhase == GamePhase.DISPLAY) {
                        timeLeft = 15*20;
                        timeLeft = 50;
                        timeLeft = DISPLAY_TIME;
                    }
                    else {
                        timeLeft = 100;
                        timeLeft = JUDGE_TIME;
                    }

                }
                else {
                    timeLeft = BUILD_TIME - currentRound*40 ; //todo 逐渐减少
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
                        int offset = playerData.getOffset();
                        Location buildLocation = offset(BUILD_LOCATION, offset);
                        if (allCorrect(buildLocation, currentBuildStructure)) {
                            playerData.setAllCorrect(true);
                            playerData.setCompletionTicks(totalTime - timeLeft);
                            UUID uuid = playerData.getUuid();
                            Player player = Bukkit.getPlayer(uuid);
                            Bukkit.broadcastMessage(ChatColor.GREEN + playerData.getPlayerName() + ChatColor.WHITE + "在" + ChatColor.YELLOW + (float) playerData.getCompletionTicks() / 20 + "s" + ChatColor.WHITE + "内完成");
                            boolean isNewPB = ConfigManager.tryUpdatePersonalBestTime(playerData.getUuid(), currentBuildName, playerData.getCompletionTicks());
                            if (isNewPB) {
                                player.sendMessage(ChatColor.LIGHT_PURPLE + "新的记录!");
                            }
                            else {
                                player.sendMessage(ChatColor.DARK_GRAY + "您的最佳记录:" + ChatColor.GRAY + (float) ConfigManager.getPersonalBestTime(uuid, currentBuildName) / 20 + "s");
                            }

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

//    public static void displayStructure(PlayerData playerData) {
//        int offset = playerData.getOffset();
//        UUID uuid = playerData.getUuid();
//        Location location = offset(BUILD_LOCATION, offset);
//        BlockEdit.pasteBuildStructure(location, currentBuildStructure, currentIsLLB);
//    }

    public static void fillAir(Location buildLocation) {
        {
            int x = buildLocation.getBlockX();
            int y = buildLocation.getBlockY();
            int z = buildLocation.getBlockZ();
            for (int dy = 0; dy < 7; dy ++) {
                for (int dx = 0; dx < 7; dx ++) {
                    for (int dz = 0; dz < 7; dz ++) {
//                                        Location locationNew = new Location(OVERWORLD, x+dx, y+dy, z+dz);
//                                        locationNew.getBlock().setType(Material.AIR, false);

                        Block block = new Location(OVERWORLD, x + dx, y + dy, z + dz).getBlock();
//                                        BlockState state = block.getState();
//                                        state.setType(Material.AIR);
//                                        // 第二个参数 false 表示不应用物理/方块更新
//                                        state.update(true, false);
//
                        block.setBlockData(Bukkit.createBlockData(Material.AIR),false);
                    }
                }
            }
        }
    }

    public static void destroyPlatform(Location buildLocation) {
        {
            int x = buildLocation.getBlockX() - 4;
            int y = buildLocation.getBlockY() - 1;
            int z = buildLocation.getBlockZ() - 4;
            for (int dy = 0; dy < 8; dy ++) {
                for (int dx = 0; dx < 15; dx ++) {
                    for (int dz = 0; dz < 15; dz ++) {
//                                        Location locationNew = new Location(OVERWORLD, x+dx, y+dy, z+dz);
//                                        locationNew.getBlock().setType(Material.AIR, false);

                        Block block = new Location(OVERWORLD, x + dx, y + dy, z + dz).getBlock();
//                                        BlockState state = block.getState();
//                                        state.setType(Material.AIR);
//                                        // 第二个参数 false 表示不应用物理/方块更新
//                                        state.update(true, false);
//
                        block.setBlockData(Bukkit.createBlockData(Material.AIR),false);
                    }
                }
            }
        }
    }

    public static void end() {
        currentPhase = GamePhase.JUDGE;
        gameLoopTask.cancel();
        ToolClass.playing = false;
        player_data.clear();
        currentRound = 0;
        for (Player player : Bukkit.getOnlinePlayers()) {
            if (player.getGameMode() == GameMode.SPECTATOR) {
                PlayerJoinEventListener.resetPlayer(player);
            }
        }
    }

    public static void displayTime(Player player, GamePhase gamePhase, int time) {

    }


    public static boolean allCorrect(Location buildLocation, Structure structure) {
        List<BlockState> blockStates = structure.getPalettes().getFirst().getBlocks();

        for (BlockState blockState : blockStates) {
            if (blockState.getY() == 0) continue;
            if (blockState.getType() == Material.AIR) continue;  //你猜猜多放方块给多少分？100分！那不就是了
            Location location = blockState.getLocation();
            location.setWorld(GameLoop.OVERWORLD);
            location.add(0, -1, 0);
            location.add(buildLocation);

            Block worldBlock = location.getBlock();
            if (!GameLoop.isBlockSame(blockState, worldBlock.getState())) {
                return false;
            }
        }
        return true;
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
                        if (displayBlock.getType() == Material.AIR) continue;  //你猜猜多放方块给多少分？100分！那不就是了

                        Location worldLocation = new Location(OVERWORLD, x + dx, y + dy, z + dz);
                        Block worldBlock = worldLocation.getBlock();

                        if (!isBlockSame(displayBlock, worldBlock)) return false;

                    }
                }
            }
        }
        return true;
    }

    public static boolean isBlockSame(BlockState displayBlock, BlockState worldBlock) {
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
//        return location.clone().offset(STEP*offset, 0, 0).toLocation(location.getWorld());

// 在 X, Y, Z 轴上分别偏移 3, 5, -2 个方块
        return offset(location, STEP*offset, 0, 0);

    }

    public static Location offset(Location location, int x, int y, int z) {
//        return location.clone().offset(x, y, z).toLocation(location.getWorld());

        return location.clone().add(x, y, z);
    }

    public static Location offset(Location location, double x, double y, double z) {
//        return location.clone().offset(x, y, z).toLocation(location.getWorld());

        return location.clone().add(x, y, z);
    }

    public static Location toTeleportLocation(Location location) {
        return offset(location, 0.5, 0, 0.5);
    }

    public static Location toTeleportLocationAndPreventStruck(Location location) {
        return offset(location, 0.5, 0, -0.5);
    }

    public static File currentBuildFile;
    public static Structure currentBuildStructure;
    public static String currentBuildFileName;
    public static String currentBuildName;

    public static void generateNewBuild() {
        if (leftStructureFile.isEmpty()) {
            leftStructureFile = new HashSet<>(BlockEdit.getAllStructureFiles());
        }


        List<File> leftStructureFileList = new ArrayList<File>(leftStructureFile);
        File file;
        int random = RandomUtils.random(0,  leftStructureFileList.size() - 1);

        file = leftStructureFileList.get(random);
        leftStructureFile.remove(file);

        currentBuildFile = file;
        currentBuildFileName = file.getName();
        currentBuildName = ConfigU.getParentPath(file.getName());


        try {
            currentBuildStructure = Bukkit.getStructureManager().loadStructure(file);
        }
        catch (IOException e) {
            throw new RuntimeException(e);
        }
    }


    public static Location BUILD_LOCATION = new Location(Bukkit.getWorld("world"), -3, -60, -3);
    public static Location DISPLAY_LOCATION = offset(BUILD_LOCATION, 0, 0, 20);
    public static Location CREATE_LOCATION = offset(BUILD_LOCATION, 0, 210, 0);
    public static Location SPAWN_LOCATION = CREATE_LOCATION.clone();
    public static Location TRAIN_LOCATION = offset(BUILD_LOCATION, 0, 160, 0);
    public static Location STRUCTURE_CACHE_LOCATION = offset(BUILD_LOCATION, 0, 260, 0); //200格



    public enum GamePhase {
        DISPLAY,
        BUILD,
        JUDGE,
    }
}
