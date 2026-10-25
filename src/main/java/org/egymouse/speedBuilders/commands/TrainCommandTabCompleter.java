package org.egymouse.speedBuilders.commands;

import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.command.TabCompleter;
import org.egymouse.speedBuilders.ToolClass;

import java.util.ArrayList;
import java.util.List;

public class TrainCommandTabCompleter implements TabCompleter {
    @Override
    public List<String> onTabComplete(CommandSender sender, Command command, String alias, String[] args) {
        List<String> completions = new ArrayList<>();
        
        // 假设你的命令是 /mycmd <子命令> [参数]
        if (args.length == 1) {
            // 玩家正在输入第一个参数（子命令）
//            completions.add("start");
//            completions.add("stop");
//            completions.add("reload");

            completions.addAll(ToolClass.allStructureBuildNames);
        }



//        } else if (args.length == 2) {
//            // 玩家正在输入第二个参数，可以根据第一个参数的值来提供不同的补全
//            if (args[0].equalsIgnoreCase("start")) {
//                completions.add("normal");
//                completions.add("hardcore");
//            } else if (args[0].equalsIgnoreCase("stop")) {
//                completions.add("now");
//                completions.add("after");
//            }
//        }
        
        // 返回的 List 就是玩家按下 Tab 键后看到的候选列表
        return completions; 
    }
}