package hdhotdog.advi.plugins.tabu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerRunnable implements Runnable {
    TabuGame game;
    TabuTimer timer;
    TabuPlayer player;
    volatile boolean shutdown = false;
    static int count = 0;
    public TimerRunnable(TabuGame game, TabuTimer timer) {
        this.game = game;
        this.timer = timer;
        this.player = timer.getCurrentTabuPlayer();
        this.timer.round++;
    }
    @Override
    public void run() {
        while(!shutdown) {
            count++;
            game.sendMessageToAllPlayers(game.prefix() + "Noch " + (120 - (30 * count)) + " Sekunden");
            game.sendMessageToAllPlayers(count + ": ");
            if (count == 4) {
                stop();
            }
        }
    }
    public void stop() {
        game.sendMessageToAllPlayers("Die Runde ist vorbei!");
        shutdown = true;
    }
}
