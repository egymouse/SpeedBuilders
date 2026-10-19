package org.egymouse.speedBuilders;

import java.util.UUID;

public class PlayerData {
    UUID uuid;
    int completionTicks = 0;

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
