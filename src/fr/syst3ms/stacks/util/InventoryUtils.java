package fr.syst3ms.stacks.util;

import org.bukkit.Material;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.PlayerInventory;
import org.bukkit.inventory.meta.ItemMeta;

import java.util.Arrays;
import java.util.List;

/**
 * Created by ARTHUR on 30/08/2017.
 */
public class InventoryUtils {
    public static boolean hasEnoughRoom(PlayerInventory inv, List<ItemStack> items) {
        return Arrays.stream(inv.getContents()).filter(i -> i != null && i.getType() != Material.AIR &&
                                                            !Arrays.asList(inv.getArmorContents()).contains(i)).count() +
               items.size() <= 36;
    }

    public static ItemStack getItem(ItemStack i, String name) {
        ItemMeta meta = i.getItemMeta();
        meta.setDisplayName(name);
        i.setItemMeta(meta);
        return i;
    }
}
