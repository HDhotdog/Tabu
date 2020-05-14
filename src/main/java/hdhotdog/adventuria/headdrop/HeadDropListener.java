package hdhotdog.adventuria.headdrop;

import hdhotdog.adventuria.Main;
import org.bukkit.ChatColor;
import org.bukkit.Location;
import org.bukkit.Material;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.PlayerDeathEvent;
import org.bukkit.inventory.ItemStack;
import org.bukkit.inventory.meta.SkullMeta;

import java.util.Random;


public class HeadDropListener implements Listener {
    private Main plugin;
    public HeadDropListener(Main plugin) {
        this.plugin = plugin;
    }
    @EventHandler
    public void onPlayerKill(PlayerDeathEvent e) {
        Player killedPlayer = e.getEntity();
            Random random = new Random();
            double prob = random.nextDouble();
            if (true) {
                //killedPlayer.sendMessage(ChatColor.GREEN + " " + prob);
                Location killPos = killedPlayer.getLocation();
                ItemStack playerHead = createPlayerHead(killedPlayer.getName());
                killedPlayer.getWorld().dropItem(killPos, playerHead);
            } else {
                //killedPlayer.sendMessage(ChatColor.RED + " " + prob);
            }

    }
    public ItemStack createPlayerHead(String name) {
        ItemStack playerHead = new ItemStack(Material.LEGACY_SKULL_ITEM, 1, (short)3);
        SkullMeta sm = (SkullMeta)playerHead.getItemMeta();
        sm.setDisplayName(ChatColor.GREEN + name);
        sm.setOwner(name);
        playerHead.setItemMeta(sm);
        return playerHead;
    }

}
