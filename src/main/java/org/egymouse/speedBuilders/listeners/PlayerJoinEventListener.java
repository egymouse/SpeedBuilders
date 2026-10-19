package org.egymouse.speedBuilders.listeners;

import org.bukkit.ChatColor;
import org.bukkit.GameMode;
import org.bukkit.Location;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.egymouse.speedBuilders.ToolClass;

public class PlayerJoinEventListener implements Listener {
    @EventHandler
    public void run(PlayerJoinEvent e){
        Player p = e.getPlayer();
        if (p.getGameMode() == GameMode.CREATIVE) {
            p.sendMessage("你是创造，改成生存重进来玩");
            return;
        }
        p.teleport(new Location(p.getWorld(),0,-60,0));
        p.getInventory().clear();
        p.getInventory().setItem(0, ToolClass.create);
        p.getInventory().setItem(1, ToolClass.play);

        p.sendTitle(ChatColor.GREEN + "欢迎来到速建练习服务器",ChatColor.YELLOW + "右键对应物品可以进行游玩",10,40,10);
//        if(!p.isOp())
//            p.setGameMode(GameMode.ADVENTURE);

    }
}
