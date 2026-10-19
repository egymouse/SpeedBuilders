//package org.egymouse.speedBuilders;
//
//import net.md_5.bungee.api.ChatMessageType;
//import net.md_5.bungee.api.chat.TextComponent;
//import org.bukkit.*;
//import org.bukkit.block.structure.Mirror;
//import org.bukkit.block.structure.StructureRotation;
//import org.bukkit.entity.Player;
//import org.bukkit.structure.Structure;
//
//import java.util.Random;
//
//public class PlayQueueManager implements Runnable{
//    public int ticks = 0;
//    @Override
//    public void run() {
//        if(!ToolClass.playing){
//            for(Player p : ToolClass.wait_player){
//                if(p.isOnline()){
//                    if(ToolClass.wait_player.size() <= 1){
//                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "当前队列共有玩家人数：" + ChatColor.YELLOW +  ToolClass.wait_player.size() + ChatColor.GREEN +"，玩家数量达到2以上开始游戏倒计时"));
//                    }else{
//                        p.spigot().sendMessage(ChatMessageType.ACTION_BAR, new TextComponent(ChatColor.GREEN + "当前队列共有玩家人数：" + ChatColor.YELLOW +  ToolClass.wait_player.size() + ChatColor.GREEN +"，游戏开始倒计时：" + net.md_5.bungee.api.ChatColor.YELLOW + ToolClass.start_time));
//                    }
//                }
//            }
//            ticks++;
//            if(ticks >= 20){
//                ticks = 0;
//                if(ToolClass.wait_player.size() >= 2){
//                    ToolClass.start_time --;
//                    if(ToolClass.start_time <= 0){
//                        ToolClass.playing_player.addAll(ToolClass.wait_player);
//                        ToolClass.wait_player.clear();
//                        ToolClass.playing = true;
//                        for(int i = 0;i< ToolClass.playing_player.size();i++){
//                            for(int x = 30*i;x<=30*i+14;x++){
//                                for(int y = 201;y<=207;y++) {
//                                    for (int z = 0; z <= 14; z++) {
//                                        Bukkit.getWorld("world").setBlockData(x, y, z, Bukkit.createBlockData(Material.AIR));
//                                    }
//                                }
//                            }
//                        }
//                        for(int i = 0;i<ToolClass.playing_player.size();i++){
//                            ToolClass.playing_player.get(i).getInventory().clear();
//                            Structure l = Bukkit.getStructureManager().loadStructure(NamespacedKey.minecraft("labby"));
//                            l.place(new Location(Bukkit.getWorld("world"),30*i,200,0),false, StructureRotation.NONE, Mirror.NONE,0,1f,new Random());
//                            ToolClass.playing_player.get(i).teleport(new Location(Bukkit.getWorld("world"),30*i+3,201,3));
//                            ToolClass.playing_player.get(i).setGameMode(GameMode.ADVENTURE);
//                        }
//                    }
//                }
//            }
//        }
//
//    }
//}
