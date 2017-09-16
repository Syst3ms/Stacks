package fr.syst3ms.stacks.classes;

import org.bukkit.entity.Player;
import org.bukkit.inventory.ItemStack;

import java.util.Arrays;
import java.util.Date;
import java.util.List;

/**
 * Created by ARTHUR on 28/08/2017.
 */
public class Stack {
    private final Player player;
    private final List<ItemStack> items;
    private final Date creationTime;

    public Stack(Player player, List<ItemStack> items, Date creationTime) {
        this.player = player;
        this.items = items;
        this.creationTime = creationTime;
    }

    public List<ItemStack> getItems() {
        return items;
    }

    public Player getPlayer() {
        return player;
    }

    public Date getCreationTime() {
        return creationTime;
    }

    public String getCreationTimeString() {
        return String.format("%1$tT", creationTime);
    }

    @Override
    public String toString() {
        return "Items : " + Arrays.toString(items.toArray());
    }
}
