package hdhotdog.advi.plugins.vanish;

import hdhotdog.advi.plugins.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;

public class Vanish implements CommandExecutor {
    private final Main main;
    private final String prefix = ChatColor.RED + "[Vanish] ";
    private static ArrayList<Player> hiddenPlayers = new ArrayList<>();

    public Vanish(Main main) {
        this.main = main;
    }
    @Override
    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        if(sender instanceof Player) {
            Player player = (Player)sender;
            if(hiddenPlayers.contains(player)) {
                for (Player players : Bukkit.getOnlinePlayers()) {
                    player.hidePlayer(this.main, players);
                    hiddenPlayers.add(player);
                    player.sendMessage(prefix + "Du bist nun für andere Benutzer unsichtbar.");
                }
            } else {
                for(Player players : Bukkit.getOnlinePlayers()) {
                    player.showPlayer(this.main, players);
                    hiddenPlayers.remove(player);
                    player.sendMessage(prefix + "Du bist nun wieder sichtbar.");
                }
            }
        } else {
            sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
        }
        return true;
    }
}
