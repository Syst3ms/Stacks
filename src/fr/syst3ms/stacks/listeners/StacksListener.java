package fr.syst3ms.stacks.listeners;

import com.google.common.collect.Lists;
import fr.syst3ms.stacks.Stacks;
import fr.syst3ms.stacks.classes.Stack;
import fr.syst3ms.stacks.util.DateUtils;
import fr.syst3ms.stacks.util.InventoryUtils;
import fr.syst3ms.stacks.util.ListUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.EntityDeathEvent;
import org.bukkit.event.inventory.InventoryClickEvent;
import org.bukkit.event.inventory.InventoryCloseEvent;
import org.bukkit.inventory.Inventory;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.ItemMeta;

import java.time.Instant;
import java.util.*;
import java.util.concurrent.TimeUnit;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

/**
 * Created by ARTHUR on 28/08/2017.
 */
public class StacksListener implements Listener {
    public static final Pattern MULTIPAGE_NAME_PATTERN = Pattern.compile("Vos stacks \\((\\d+)\\)");

    private static Map<Player, List<Stack>> stackMap = new HashMap<>();
    private static Map<Player, Map<Integer, Inventory>> playerPageMap = new HashMap<>();
    private static LinkedHashMap<Integer, Stack> tempStackMap;

    public static Map<Player, Map<Integer, Inventory>> getPlayerPageMap() {
        return playerPageMap;
    }

    public static void setTempStackMap(LinkedHashMap<Integer, Stack> tempStackMap) {
        StacksListener.tempStackMap = tempStackMap;
    }

    public static Map<Player, List<Stack>> getStackMap() {
        return stackMap;
    }

    @SuppressWarnings("ArraysAsListWithZeroOrOneArgument")
    @EventHandler
    public void onEntityDeath(EntityDeathEvent event) {
        if (event.getEntity().getKiller() != null) {
            Player killer = event.getEntity().getKiller();
            List<ItemStack> drops = new ArrayList<>(event.getDrops());
            if (drops.isEmpty()) {
                return;
            }
            event.getDrops().clear();
            Map<Player, List<Stack>> stackMap = StacksListener.stackMap;
            if (!stackMap.containsKey(killer)) {
                Stack stack = new Stack(killer, drops, Date.from(Instant.now()));
                stackMap.put(killer, new ArrayList<>(Arrays.asList(stack)));
            } else {    
                List<Stack> current = stackMap.get(killer);
                assert current != null;
                Stack stack = new Stack(killer, drops, Date.from(Instant.now()));
                current.add(stack);
                stackMap.put(killer, new ArrayList<>(current));
            }
            recalculateInventories(killer);
        }
    }

    @EventHandler
    public void onInventoryClick(InventoryClickEvent event) {
        String name = event.getClickedInventory().getName();
        if (name.startsWith("Vos stacks")) {
            event.setCancelled(true);
            Player p = (Player) event.getWhoClicked();
            ItemStack item = event.getCurrentItem();
            Map<Integer, Inventory> pageMap = playerPageMap.get(p);
            Matcher m = MULTIPAGE_NAME_PATTERN.matcher(event.getInventory().getName());
            int slot = event.getSlot();
            if (m.matches()) {
                int pageIndex = Integer.parseInt(m.group(1)) - 1;
                Inventory inv = pageMap.get(pageIndex);
                assert inv != null;
                if (item.getType() == Material.ARROW) {
                    Bukkit.getScheduler().runTaskLater(
                            Bukkit.getPluginManager().getPlugin("Stacks"),
                            p::closeInventory,
                            1
                    );
                    recalculateInventories(p);
                    Bukkit.getScheduler().runTaskLater(
                            Bukkit.getPluginManager().getPlugin("Stacks"),
                            () -> {
                                Inventory page = pageMap.get(
                                        pageIndex +
                                        (item.getItemMeta().getDisplayName().equals("Page suivante") ? 1 : -1));
                                if (page == null)
                                    return;
                                p.openInventory(page);
                                tempStackMap = ListUtils.mapFromList(StacksListener.stackMap.get(p));
                            },
                            1
                    );
                } else if (item.getType() == Material.CHEST) {
                    Stack stack = tempStackMap.remove(slot);
                    assert stack != null;
                    if (!InventoryUtils.hasEnoughRoom(p.getInventory(), stack.getItems())) {
                        p.sendMessage(Stacks.PREFIX +
                                      ChatColor.RED +
                                      "Vous n'avez pas assez de place dans votre inventaire !");
                        Bukkit.getScheduler().runTaskLater(
                                Bukkit.getPluginManager().getPlugin("Stacks"),
                                p::closeInventory,
                                1
                        );
                        return;
                    }
                    event.setCurrentItem(new ItemStack(Material.AIR));
                    p.getInventory().addItem(stack.getItems().toArray(new ItemStack[0]));
                }
            } else if (item.getType() == Material.CHEST) {
                Stack stack = tempStackMap.remove(slot);
                assert stack != null;
                if (!InventoryUtils.hasEnoughRoom(p.getInventory(), stack.getItems())) {
                    p.sendMessage(Stacks.PREFIX +
                                  ChatColor.RED +
                                  "Vous n'avez pas assez de place dans votre inventaire !");
                    Bukkit.getScheduler().runTaskLater(
                            Bukkit.getPluginManager().getPlugin("Stacks"),
                            p::closeInventory,
                            1
                    );
                    return;
                }
                event.setCurrentItem(new ItemStack(Material.AIR));
                p.getInventory().addItem(stack.getItems().toArray(new ItemStack[0]));
            }
        }
    }

    @EventHandler
    public void onInventoryClose(InventoryCloseEvent event) {
        if (event.getInventory().getName().startsWith("Vos stacks")) {
            Player p = (Player) event.getPlayer();
            stackMap.remove(p);
            if (!tempStackMap.isEmpty()) {
                stackMap.put(p, new ArrayList<>(tempStackMap.values()));
            }
            recalculateInventories(p);
            tempStackMap = null;
        }
    }

    public void recalculateInventories(Player p) {
        List<Stack> stacks = stackMap.get(p);
        stacks.removeIf(s -> DateUtils.getDateDiff(
                s.getCreationTime(),
                Date.from(Instant.now()),
                TimeUnit.MINUTES
        ) > 60);
        List<ItemStack> stackItems = new ArrayList<>();
        for (int i = 1; i <= stacks.size(); i++) {
            Stack stack = stacks.get(i - 1);
            ItemStack item = new ItemStack(Material.CHEST);
            ItemMeta meta = item.getItemMeta();
            meta.setLore(
                    Arrays.asList(
                            "Créé à " + stack.getCreationTimeString() + ".",
                            "Expire dans " +
                            (60 - DateUtils.getDateDiff(stack.getCreationTime(), Date.from(Instant.now()),
                                    TimeUnit.MINUTES
                            )) +
                            " minute(s)."
                    )
            );
            item.setItemMeta(meta);
            stackItems.add(item);
        }
        if (stackItems.size() > 54) {
            List<List<ItemStack>> partitioned = Lists.partition(stackItems, 45);
            Map<Integer, Inventory> pageMap = new HashMap<>();
            for (int i = 0; i < partitioned.size(); i++) {
                int page = i + 1;
                Inventory inv = Bukkit.createInventory(p, 54, "Vos stacks (" + page + ")");
                List<ItemStack> currentPage = partitioned.get(i);
                for (int j = 0; j < currentPage.size(); j++) {
                    ItemStack it = currentPage.get(j);
                    inv.setItem(j, it);
                }
                if (i == 0) {
                    ItemStack nextArrow = new ItemStack(Material.ARROW);
                    ItemMeta meta = nextArrow.getItemMeta();
                    meta.setDisplayName("Page suivante");
                    nextArrow.setItemMeta(meta);
                    inv.setItem(53, nextArrow);
                } else if (i + 1 >= partitioned.size()) {
                    ItemStack backArrow = new ItemStack(Material.ARROW);
                    ItemMeta backMeta = backArrow.getItemMeta();
                    backMeta.setDisplayName("Page précédente");
                    backArrow.setItemMeta(backMeta);
                    inv.setItem(45, backArrow);
                } else {
                    ItemStack backArrow = new ItemStack(Material.ARROW), nextArrow = new ItemStack(Material.ARROW);
                    ItemMeta nextMeta = nextArrow.getItemMeta(), backMeta = backArrow.getItemMeta();
                    nextMeta.setDisplayName("Page suivante");
                    nextArrow.setItemMeta(nextMeta);
                    backMeta.setDisplayName("Page précédente");
                    backArrow.setItemMeta(backMeta);
                    inv.setItem(45, backArrow);
                    inv.setItem(53, nextArrow);
                }
                pageMap.put(i, inv);
            }
            playerPageMap.put(p, pageMap);
        } else {
            Inventory inv = Bukkit.createInventory(p, (stackItems.size() / 9 + 1) * 9, "Vos stacks");
            for (int i = 0; i < stackItems.size(); i++) {
                inv.setItem(i, stackItems.get(i));
            }
            Map<Integer, Inventory> pageMap = new HashMap<>(Collections.singletonMap(0, inv));
            playerPageMap.put(p, pageMap);
        }
    }
}
