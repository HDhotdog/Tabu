package hdhotdog.advi.plugins;

import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Enabled");

        //FileConfiguration wordFile = loadWordFile();
        this.getServer().getPluginManager().registerEvents(new HeadDropListener(this), this);
        this.getServer().getPluginManager().registerEvents(new Tabu(this), this);
        this.getCommand("tabu").setExecutor(new Tabu(this));

    }
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "Disabled");
    }
}
