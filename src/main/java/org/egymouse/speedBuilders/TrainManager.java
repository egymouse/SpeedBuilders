package org.egymouse.speedBuilders;

import net.md_5.bungee.api.ChatMessageType;
import net.md_5.bungee.api.chat.TextComponent;
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

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class TrainManager {
    public static HashMap<UUID, TrainPlayerData> player_trainData = new HashMap<>();
    public static int maxOffset = 0;




    public static void tryRegisterPlayer(Player player, Structure structure, String buildName) {
        UUID uuid = player.getUniqueId();
        TrainPlayerData trainPlayerData;
        Location location;
        if (player_trainData.containsKey(uuid)) {
            trainPlayerData = player_trainData.get(uuid);
            trainPlayerData.setCurrentStructure(structure);
            int offset = trainPlayerData.getOffset();

            location = GameLoop.offset(GameLoop.TRAIN_LOCATION,  offset);
        }
        else {
            boolean[] used = new boolean[maxOffset + 1];

            int unusedOffset = maxOffset + 1;
            for (TrainPlayerData allTrainPlayerData : player_trainData.values()) {
                int offset = allTrainPlayerData.getOffset();
                used[offset] = true;

                for (int i = 0; i <= maxOffset ; i++) {
                    if (!used[i]) {
                        unusedOffset = i;
                        break;
                    }
                }
            }
            if (unusedOffset > maxOffset) {
                maxOffset = unusedOffset;
            }
            trainPlayerData = new TrainPlayerData(uuid);
            trainPlayerData.setPlayerName(player.getName());
            trainPlayerData.setOffset(unusedOffset);
            trainPlayerData.setCurrentStructure(structure);
            player_trainData.put(uuid, trainPlayerData);

            location = GameLoop.offset(GameLoop.TRAIN_LOCATION,  unusedOffset);
            BlockEdit.pasteArena(location);
            player.teleport(location);
        }
        player.getInventory().clear();
        player.setGameMode(GameMode.SURVIVAL);
        BlockEdit.pasteBuildStructure(location, structure);

        List<BlockState> blockStates = structure.getPalettes().getFirst().getBlocks();
        int totalBlockCount = 0;
        Inventory inventory = player.getInventory();
        Inventory defaultInventory = Bukkit.createInventory(null, 36);

        for (BlockState blockState : blockStates) {
            if (blockState.getY() == 0) continue;
            Material material = blockState.getType();
            // 跳过空气
            if (material == Material.AIR) continue;
            totalBlockCount++;

            BlockData blockData = blockState.getBlockData();

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
            defaultInventory.addItem(itemStack);
        }

        trainPlayerData.setInventory(defaultInventory);

        trainPlayerData.setTotalBlockCount(totalBlockCount);
        trainPlayerData.setBuildName(buildName);
        player_trainData.put(uuid, trainPlayerData);

        player.sendMessage(ChatColor.YELLOW + "输入/train开始练习");

    }



    public static void tryUnregisterPlayer(Player player) {
        UUID uuid = player.getUniqueId();
        TrainPlayerData trainPlayerData = player_trainData.remove(uuid);
        if (trainPlayerData == null) return;
        BukkitTask bukkitTask = trainPlayerData.getBukkitTask();
        if (bukkitTask != null) {
            bukkitTask.cancel();
        }
        Location location = GameLoop.offset(GameLoop.TRAIN_LOCATION, trainPlayerData.getOffset());
        GameLoop.destroyPlatform(location);
        PlayerJoinEventListener.resetPlayer(player);
    }

    public static int DISPLAY_TIME = 60;

    public static void startTrain(Player player) {
        UUID uuid = player.getUniqueId();
        TrainPlayerData finalTrainPlayerData = player_trainData.get(uuid);
        BukkitTask oldBukkitTask = finalTrainPlayerData.getBukkitTask();
        if (oldBukkitTask != null) {
            trainEnd(player);
            BlockEdit.pasteBuildStructure(GameLoop.offset(GameLoop.TRAIN_LOCATION, finalTrainPlayerData.getOffset()), finalTrainPlayerData.getCurrentStructure());
            //fvv玩家没建完，还需要我自己放置结构
            player.sendMessage(ChatColor.YELLOW + "我知道你很急，但请调整好心情(和物品栏)，输入/train再次开始练习");
            return;
        }
        String buildName = finalTrainPlayerData.getBuildName();

        int totalTime = DISPLAY_TIME;
        finalTrainPlayerData.setTime(totalTime);


        int playerBestTime = ConfigManager.getPersonalBestTime(uuid, buildName);
        if (playerBestTime == 0) {
            playerBestTime = GameLoop.BUILD_TIME;
        }
        int realPlayerBestTime = playerBestTime;

        BukkitScheduler bukkitScheduler = Bukkit.getScheduler();
        BukkitTask bukkitTask = bukkitScheduler.runTaskTimer(SpeedBuilders.getPlugin(), () -> {
            TrainPlayerData trainPlayerData = finalTrainPlayerData;
            GameLoop.GamePhase currentPhase = trainPlayerData.getCurrentPhase();
            int timeLeft = trainPlayerData.getTime();

            double remain = (double) timeLeft / 20.0;
//                String timeStr = String.format(java.util.Locale.ROOT, "%.1f", remain);
            String timeStr = String.format(java.util.Locale.ROOT, "%05.2f", remain);
            String phaseString = "复原";
            if (currentPhase == GameLoop.GamePhase.DISPLAY) {
                phaseString = "剩余观察";
            }
            if (currentPhase != GameLoop.GamePhase.JUDGE) {
                player.spigot().sendMessage(
                        ChatMessageType.ACTION_BAR,
                        new TextComponent(ChatColor.GREEN + phaseString + "时间：" + ChatColor.YELLOW + timeStr)
                );
            }
            if (timeLeft >= 0) {
                player.setLevel(Math.ceilDiv(timeLeft, 20));
                if (currentPhase == GameLoop.GamePhase.DISPLAY) {
                    player.setExp((float) timeLeft / totalTime);
                }
                else {
                    player.setExp(Math.min((float) timeLeft / realPlayerBestTime, 1));
                }

            }

            if (currentPhase == GameLoop.GamePhase.DISPLAY) {

                timeLeft--;
                if (timeLeft <= 0) {
                    trainPlayerData.setCurrentPhase(GameLoop.GamePhase.BUILD);
                    trainPlayerData.setTime(timeLeft);

                    int offset = trainPlayerData.getOffset();
                    Location location = GameLoop.offset(GameLoop.TRAIN_LOCATION, offset);
                    GameLoop.fillAir(location);
                }

            }
            else {
                timeLeft++;

                int offset = trainPlayerData.getOffset();
                Location buildLocation = GameLoop.offset(GameLoop.TRAIN_LOCATION, offset);

                Structure structure = trainPlayerData.getCurrentStructure();
                boolean allCorrect = GameLoop.allCorrect(buildLocation, structure);

                if (allCorrect) {
                    player.sendMessage(ChatColor.GREEN + "总方块数: " + ChatColor.YELLOW + trainPlayerData.getTotalBlockCount());

                    List<Integer> blockRightDuringPhase = trainPlayerData.getBlockRightDuringPhase();
                    for (int i = 0; i < blockRightDuringPhase.size(); i++) {
                        int blockRight = blockRightDuringPhase.get(i);
                        player.sendMessage(ChatColor.GREEN + String.valueOf((i+1)*10) + "s: " + ChatColor.YELLOW + blockRight + "对");
                    }

                    player.sendMessage(ChatColor.GREEN + trainPlayerData.getPlayerName() + ChatColor.WHITE + "在" + ChatColor.YELLOW + (float)timeLeft / 20 + "s" + ChatColor.WHITE + "内完成");
                    boolean isNewPB = ConfigManager.tryUpdatePersonalBestTime(uuid, buildName, timeLeft);
                    if (isNewPB) {
                        player.sendMessage(ChatColor.LIGHT_PURPLE + "新的记录!");
                    }
                    else {
                        player.sendMessage(ChatColor.DARK_GRAY + "您的最佳记录:" + ChatColor.GRAY + (float) ConfigManager.getPersonalBestTime(uuid, buildName) / 20 + "s");
                    }
                    player.sendMessage(ChatColor.YELLOW + "输入/train重复练习");



                    trainEnd(player);
                }
                else {
                    int correctBlocks = 0;
                    if (timeLeft % 200 == 0) {
                        {


                            List<BlockState> blockStates = structure.getPalettes().getFirst().getBlocks();

                            for (BlockState blockState : blockStates) {
                                if (blockState.getY() == 0) continue;
                                if (blockState.getType() == Material.AIR) continue;
                                Location location = blockState.getLocation();
                                location.setWorld(GameLoop.OVERWORLD);
                                location.add(0, -1, 0);
                                location.add(buildLocation);

                                Block worldBlock = location.getBlock();
                                if (GameLoop.isBlockSame(blockState, worldBlock.getState())) {
                                    correctBlocks++;
                                }
                            }
                        }
                        trainPlayerData.addBlockRightCount(correctBlocks);
                    }
                }

            }
            trainPlayerData.setTime(timeLeft);

//            player_trainData.put(uuid, trainPlayerData);
        }, 1, 1);
        finalTrainPlayerData.setBukkitTask(bukkitTask);
    }

    public static void trainEnd(Player player) {
        UUID uuid = player.getUniqueId();
        TrainPlayerData trainPlayerData = player_trainData.get(uuid);
        trainPlayerData.setTime(DISPLAY_TIME);
        trainPlayerData.setCurrentPhase(GameLoop.GamePhase.DISPLAY);
        trainPlayerData.getBukkitTask().cancel();
        trainPlayerData.setBukkitTask(null);  //内存泄露大神们
        trainPlayerData.getBlockRightDuringPhase().clear();

        Inventory defaultInventory = trainPlayerData.getInventory();
        Inventory playerInventory = player.getInventory();
        playerInventory.clear();
        for (int i = 0; i < defaultInventory.getSize(); i++) {
            playerInventory.setItem(i, defaultInventory.getItem(i));
        }
    }
}
