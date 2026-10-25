package org.egymouse.speedBuilders;

import org.ajls.lib.utils.ItemStackU;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.NamespacedKey;
import org.bukkit.block.BlockFace;
import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;
import org.bukkit.persistence.PersistentDataType;
import org.bukkit.structure.Structure;

import java.util.*;

public class ToolClass {
    public static ItemStack create = new ItemStack(Material.SLIME_BALL);
    public static ItemStack play = new ItemStack(Material.MAGMA_CREAM);
    public static ItemStack start = new ItemStack(Material.EMERALD);
    public static ItemStack cancel = new ItemStack(Material.STICK);

    public static Structure create_structure = null;
    public static boolean playing = false;
    public static Set<Player> wait_player = new HashSet<>();
    public static int start_time = 30;
    public static Map<UUID,Integer> destroy_tick = new HashMap<>();

    public static List<String> allStructureFileNames = BlockEdit.getAllStructureFileNames();
    public static List<String> allStructureBuildNames = BlockEdit.getAllStructureBuildNames();

    public static void Init(){
        ItemMeta meta = create.getItemMeta();
        assert meta != null;
        meta.setDisplayName(ChatColor.YELLOW + "出题");
        meta.setLore(List.of(ChatColor.GREEN + "右键前往出题"));
        create.setItemMeta(meta);
        setPluginItem(create);

        ItemMeta meta1 = play.getItemMeta();
        assert meta1 != null;
        meta1.setDisplayName(ChatColor.YELLOW + "游玩");
        meta1.setLore(List.of(ChatColor.GREEN + "右键进行准备"));
        play.setItemMeta(meta1);
        setPluginItem(play);

        meta1 = cancel.getItemMeta();
        assert meta1 != null;
        meta1.setDisplayName(ChatColor.YELLOW + "退出队列");
        meta1.setLore(List.of(ChatColor.GREEN + "右键进行退出"));
        cancel.setItemMeta(meta1);
        setPluginItem(cancel);

        meta1 = start.getItemMeta();
        assert meta1 != null;
        meta1.setDisplayName(ChatColor.YELLOW + "开始游戏");
        meta1.setLore(List.of(ChatColor.GREEN + "右键"));
        start.setItemMeta(meta1);
        setPluginItem(start);



    }

    public static void setPluginItem(ItemStack itemStack) {
        ItemStackU.setPersistentData(itemStack, NameSpacedKeys.PLUGIN_ITEM, PersistentDataType.BOOLEAN, true);
    }

    public static boolean isPluginItem(ItemStack itemStack) {
        return ItemStackU.hasPersistentData(itemStack, NameSpacedKeys.PLUGIN_ITEM, PersistentDataType.BOOLEAN);
    }


}
