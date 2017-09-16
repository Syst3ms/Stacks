package fr.syst3ms.stacks;

import fr.syst3ms.stacks.classes.Stack;
import fr.syst3ms.stacks.listeners.StacksListener;
import fr.syst3ms.stacks.util.ListUtils;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Cow;
import org.bukkit.entity.EntityType;
import org.bukkit.entity.Player;
import org.bukkit.inventory.Inventory;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.List;
import java.util.Map;

/**
 * Created by ARTHUR on 28/08/2017.
 */
public class Stacks extends JavaPlugin {
    public static final String PREFIX = ChatColor.GRAY +
                                        "[" +
                                        ChatColor.AQUA +
                                        "Stacks" +
                                        ChatColor.GRAY +
                                        "] " +
                                        ChatColor.RESET;
    private static Stacks instance;

    public static Stacks getInstance() {
        return instance;
    }

    @Override
    public void onEnable() {
        if (instance != null) {
            Bukkit.getPluginManager().disablePlugin(this);
            throw new IllegalStateException(PREFIX + "Impossible de créer deux instances du plugin !");
        }
        instance = this;
        Bukkit.getPluginManager().registerEvents(new StacksListener(), this);
    }

    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if (command.getName().equalsIgnoreCase("stacks")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(PREFIX +
                                   ChatColor.RED +
                                   "La commande /stacks ne peut être exécutée que par un joueur !");
                return true;
            }
            Player p = (Player) sender;
            List<Stack> stacks = StacksListener.getStackMap().get(p);
            if (stacks == null) {
                p.sendMessage(PREFIX + ChatColor.RED + "Vous n'avez aucun stack !");
                return true;
            }
            Map<Integer, Inventory> pageMap = StacksListener.getPlayerPageMap().get(p);
            p.openInventory(pageMap.get(0));
            StacksListener.setTempStackMap(ListUtils.mapFromList(StacksListener.getStackMap().get(p)));
            return true;
        } else if (command.getName().equalsIgnoreCase("sf")) {
            if (!(sender instanceof Player)) {
                sender.sendMessage(PREFIX +
                                   ChatColor.RED +
                                   "La commande /sf ne peut être exécutée que par un joueur !");
                return true;
            }
            Player p = (Player) sender;
            for (int i = 0; i < 64; i++) {
                Bukkit.getScheduler().runTask(this, () -> {
                    p.getWorld().spawnEntity(p.getLocation(), EntityType.COW);
                    p.performCommand("entitydata @e[c=1,type=Cow] {Silent:1b}");
                });
            }
        }
        return false;
    }

}
