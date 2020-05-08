package hdhotdog.advi.plugins;

import org.bukkit.ChatColor;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.configuration.file.YamlConfiguration;
import org.bukkit.plugin.java.JavaPlugin;

import java.io.File;
import java.io.IOException;

public class Main extends JavaPlugin {

    private File file;
    private FileConfiguration words;
    public FileConfiguration loadWordFile() {
        file = new File(getDataFolder(), "words.txt");
        if(!file.exists()) {
            file.getParentFile().mkdirs();
            try{
                file.createNewFile();
            } catch (IOException e) {
                e.printStackTrace();
            }
        }
        words = YamlConfiguration.loadConfiguration(file);
        return words;
    }
    @Override
    public void onEnable() {
        getServer().getConsoleSender().sendMessage(ChatColor.GREEN + "Enabled");

        //FileConfiguration wordFile = loadWordFile();
        this.getServer().getPluginManager().registerEvents(new HeadDropListener(this), this);
        this.getServer().getPluginManager().registerEvents(new Tabu(), this);
        this.getCommand("tabu").setExecutor(new Tabu());

    }
    public void onDisable() {
        getServer().getConsoleSender().sendMessage(ChatColor.RED + "Disabled");
    }
}
