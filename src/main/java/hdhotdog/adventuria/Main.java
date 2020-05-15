package hdhotdog.adventuria;

import hdhotdog.adventuria.tabu.Tabu;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

import java.util.Objects;


public class Main extends JavaPlugin {


    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "[TABU]" + ChatColor.GREEN + " enabled");
        this.getServer().getPluginManager().registerEvents(new Tabu(this), this);
        Objects.requireNonNull(this.getCommand("tabu")).setExecutor(new Tabu(this));
    }
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.BLUE + "[TABU]" + ChatColor.RED + " disabled");
    }

}
