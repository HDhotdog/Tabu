package hdhotdog.advi.plugins.tabu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerRunnable implements Runnable {
    TabuGame game;
    TabuTimer timer;
    TabuPlayer player;
    static int count = 0;
    Thread thread;
    public TimerRunnable(TabuGame game, TabuTimer timer) {
        this.game = game;
        this.timer = timer;
        this.player = timer.getCurrentTabuPlayer();
        this.timer.round++;
        thread = new Thread(this);
        thread.start();
    }

    public void run() {
        try {
            this.wait(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        count = 0;
        thread.stop();
        game.sendMessageToAllPlayers("Die Runde ist vorbei!");
    }
}
