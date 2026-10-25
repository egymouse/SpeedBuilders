package org.egymouse.speedBuilders;

import org.bukkit.Location;

import java.util.UUID;

public class PlayerData {


    public Location getBuildLocation() {
        return GameLoop.offset(GameLoop.BUILD_LOCATION, offset);
    }
    UUID uuid;
    int completionTicks = 0;

    public String getPlayerName() {
        return playerName;
    }

    public void setPlayerName(String playerName) {
        this.playerName = playerName;
    }

    String playerName;

    public boolean isAllCorrect() {
        return allCorrect;
    }

    public void setAllCorrect(boolean allCorrect) {
        this.allCorrect = allCorrect;
    }

    public int getCompletionTicks() {
        return completionTicks;
    }

    public void setCompletionTicks(int completionTicks) {
        this.completionTicks = completionTicks;
    }

    boolean allCorrect = false;

    public int getBlockRight() {
        return blockRight;
    }

    public void setBlockRight(int blockRight) {
        this.blockRight = blockRight;
    }

    public int getBlockWrong() {
        return blockWrong;
    }

    public void setBlockWrong(int blockWrong) {
        this.blockWrong = blockWrong;
    }

    int blockRight;
    int blockWrong;
    public PlayerData(UUID uuid) {
         this.uuid = uuid;
    }

    int health = 3;

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

    public int getHealth() {
        return health;
    }

    public void setHealth(int health) {
        this.health = health;
    }

    int offset = 0;



    //offset 20格
}
