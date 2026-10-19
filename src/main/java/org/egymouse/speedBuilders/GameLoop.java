package org.egymouse.speedBuilders;

import org.ajls.lib.utils.RandomUtils;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Bisected;
import org.bukkit.block.data.BlockData;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.scheduler.BukkitScheduler;
import org.stringtemplate.v4.ST;

import java.util.*;

public class GameLoop {
    public static int timeLeft;
    public static GamePhase currentPhase = GamePhase.DISPLAY;
    public static Set<String> leftSchematicFileNames = new HashSet<>(BlockEdit.getAllSchematicFileNames());
    public static HashMap<UUID, PlayerData> player_data = new HashMap<>();
    public static int DEFAULT_HEALTH = 3;
    public static int STEP = 20;
    public static String areanaString = "areana";
    public static World OVERWORLD = Bukkit.getWorld("world");
    public static int TO_CENTER = 3;


    public static void start() {
        int offset = 0;
        for (Player player: Bukkit.getOnlinePlayers()) {
            PlayerData playerData = new PlayerData(player.getUniqueId());
            playerData.setOffset(offset);
            playerData.setHealth(DEFAULT_HEALTH);
            player_data.put(player.getUniqueId(), playerData);
        }

        for (int i = 0; i <= offset; i++) {
            BlockEdit.pasteSchematic(BUILD_LOCATION.clone().offset(STEP*i, 0, 0).toLocation(Bukkit.getWorld("world")), areanaString);

        }
        BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();
        scheduler.runTaskLater(SpeedBuilders.getPlugin(), () -> {
            test();
        }, 100);
    }

    public static int totalBlockCount = 0;

    public static void test() {
        timeLeft = 100;
        BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();
        scheduler.runTaskTimer(SpeedBuilders.getPlugin(), () -> {
            timeLeft --;
//            if (currentPhase == GamePhase.BUILD) {
//                //tick t oc
//            }

            if (timeLeft <= 0) {
                int currentPhaseOrdinal = currentPhase.ordinal();
                currentPhaseOrdinal ++;
                if (currentPhaseOrdinal >= GamePhase.values().length) {
                    currentPhaseOrdinal = 0;
                }

                if (currentPhase == GamePhase.DISPLAY) {
                    generateNewBuild();
                    BlockEdit.pasteSchematic(DISPLAY_LOCATION, currentBuildFileName);
                    for (PlayerData playerData : player_data.values()) {
                        int offset = playerData.getOffset();
                        Location location = offset(BUILD_LOCATION, offset);
                        BlockEdit.pasteSchematic(location, currentBuildFileName);
                    }
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
                    for (UUID uuid : player_data.keySet()) {
                        Player player = Bukkit.getPlayer(uuid);
                        if (player == null) continue;
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
                                        locationNew.getBlock().setType(Material.AIR);
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
                        int correctBlocks = 0;
                        int misplacedBlocks = 0;
                        {
                            int x = buildLocation.getBlockX();
                            int y = buildLocation.getBlockY();
                            int z = buildLocation.getBlockZ();
                            for (int dy = 0; dy < 7; dy ++) {
                                for (int dx = 0; dx < 7; dx ++) {
                                    for (int dz = 0; dz < 7; dz ++) {
                                        Location locationNew = new Location(OVERWORLD, x+dx, y+dy, z+dz);
                                        Block newBlock = locationNew.getBlock();
                                        if (newBlock.getType() == Material.AIR) continue;
                                        Location locationDisplay = offset(DISPLAY_LOCATION, dx, dy, dz);
                                        Block displayBlock = locationDisplay.getBlock();
                                        if (newBlock.getBlockData().matches(displayBlock.getBlockData())) {
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
                        playerFill(buildLocation, Material.AIR);
                    }
                    displayFill(Material.AIR);
//                    BukkitScheduler scheduler = SpeedBuilders.getPlugin().getServer().getScheduler();
                    List<PlayerData> sortedPlayerDataList = new ArrayList<>();
                    scheduler.runTaskLater(SpeedBuilders.getPlugin(), () -> {
                        Collection<PlayerData> playerDataList = player_data.values();
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
                            playerDataList.remove(bestPlayerData);
                            sortedPlayerDataList.add(bestPlayerData);
                        }
                        Bukkit.broadcastMessage(totalBlockCount + "");
                        for (int i = 0; i < sortedPlayerDataList.size(); i ++) {
                            PlayerData playerData = sortedPlayerDataList.get(i);

                            UUID uuid = playerData.getUuid();
                            Player player = Bukkit.getPlayer(uuid);
                            if (player != null) {
                                Bukkit.broadcastMessage(player.getName() + ":" + playerData.getBlockRight());
                            }
                        }
                        PlayerData lastPlayerData = sortedPlayerDataList.getLast();
                        lastPlayerData.setHealth(lastPlayerData.getHealth()-1);
                        if (lastPlayerData.getHealth() <= 0) {
                            player_data.remove(sortedPlayerDataList.getLast());
                            if (player_data.size() == 1) {
                                this.
                            }
                        }




                    }, 50);




                }

                timeLeft = 100;


            }
        }, 1, 1);

    }

    public static void displayFill(Material material) {
        {
            int x = DISPLAY_LOCATION.getBlockX() + TO_CENTER - STEP/2;
            int y = DISPLAY_LOCATION.getBlockY();
            int z = DISPLAY_LOCATION.getBlockZ() + TO_CENTER - STEP/2;
            for (int dy = 0; dy <= STEP; dy ++) {
                for (int dx = 0; dx <= STEP; dx ++) {
                    Location locationNew = new Location(OVERWORLD, x+dx, y+dy, z);
                    locationNew.getBlock().setType(material);
                }
            }
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
                    locationNew.getBlock().setType(Material.BEDROCK);
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
                    locationNew.getBlock().setType(Material.BEDROCK);
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

    public static String currentBuildFileName;

    public static void generateNewBuild() {
        if (leftSchematicFileNames.isEmpty()) {
            leftSchematicFileNames = new HashSet<>(BlockEdit.getAllSchematicFileNames());
        }


        List<String> leftSchematicFileNamesList = new ArrayList<String>(leftSchematicFileNames);
        int random = RandomUtils.random(0, leftSchematicFileNamesList.size() - 1);
        String fileName = leftSchematicFileNamesList.get(random);
        currentBuildFileName = fileName;
        leftSchematicFileNames.remove(fileName);


    }

    public static void startBuilding() {
        //todo 设置成空气，给方块
    }

    public static Location BUILD_LOCATION = new Location(Bukkit.getWorld("world"), -3, -60, -3);
    public static Location DISPLAY_LOCATION = new Location(Bukkit.getWorld("world"), -3, -60, 17);



    public enum GamePhase {
        DISPLAY,
        BUILD,
        JUDGE,
    }
}
