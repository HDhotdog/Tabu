package hdhotdog.advi.plugins;

import hdhotdog.advi.plugins.headdrop.HeadDropListener;
import hdhotdog.advi.plugins.tabu.Tabu;
import hdhotdog.advi.plugins.vanish.Vanish;
import org.bukkit.ChatColor;
import org.bukkit.plugin.java.JavaPlugin;
import org.omg.CORBA.portable.ValueInputStream;

public class Main extends JavaPlugin {
    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Enabled");

        //FileConfiguration wordFile = loadWordFile();
        this.getServer().getPluginManager().registerEvents(new HeadDropListener(this), this);
        this.getServer().getPluginManager().registerEvents(new Tabu(this), this);
        this.getCommand("tabu").setExecutor(new Tabu(this));
        this.getCommand("vanish").setExecutor(new Vanish(this));

    }
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "Disabled");
    }
}
