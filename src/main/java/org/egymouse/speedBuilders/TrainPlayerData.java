package org.egymouse.speedBuilders;

import org.bukkit.inventory.Inventory;
import org.bukkit.scheduler.BukkitTask;
import org.bukkit.structure.Structure;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class TrainPlayerData {
    UUID uuid;

    public Inventory getInventory() {
        return inventory;
    }

    public void setInventory(Inventory inventory) {
        this.inventory = inventory;
    }

    Inventory inventory;

    public BukkitTask getBukkitTask() {
        return bukkitTask;
    }

    public int getTime() {
        return time;
    }

    public void setTime(int time) {
        this.time = time;
    }

    int time = 60;


    public void setBukkitTask(BukkitTask bukkitTask) {
        this.bukkitTask = bukkitTask;
    }

    public BukkitTask bukkitTask;

    public GameLoop.GamePhase getCurrentPhase() {
        return currentPhase;
    }

    public void setCurrentPhase(GameLoop.GamePhase currentPhase) {
        this.currentPhase = currentPhase;
    }

    public GameLoop.GamePhase currentPhase = GameLoop.GamePhase.DISPLAY;



    public List<Integer> getBlockRightDuringPhase() {
        return blockRightDuringPhase;
    }

    public void setBlockRightDuringPhase(List<Integer> blockRightDuringPhase) {
        this.blockRightDuringPhase = blockRightDuringPhase;
    }

    public void addBlockRightCount(int count) {
        blockRightDuringPhase.add(count);
    }

    List<Integer> blockRightDuringPhase = new ArrayList<>();

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    String playerName;

    public TrainPlayerData(UUID uuid) {
         this.uuid = uuid;
    }


    public UUID getUuid() {
        return uuid;
    }

    public void setUuid(UUID uuid) {
        this.uuid = uuid;
    }

    public int getOffset() {
        return offset;
    }

    public void setOffset(int offset) {
        this.offset = offset;
    }

    int offset = 0;

    public Structure getCurrentStructure() {
        return currentStructure;
    }

    public void setCurrentStructure(Structure currentStructure) {
        this.currentStructure = currentStructure;
    }

    Structure currentStructure;

    public String getBuildName() {
        return buildName;
    }

    public void setBuildName(String buildName) {
        this.buildName = buildName;
    }

    String buildName;

    public int getTotalBlockCount() {
        return totalBlockCount;
    }

    public void setTotalBlockCount(int totalBlockCount) {
        this.totalBlockCount = totalBlockCount;
    }

    int totalBlockCount;




    //offset 20格
}
