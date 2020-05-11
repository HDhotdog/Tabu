package hdhotdog.advi.plugins.tabu;

import org.bukkit.ChatColor;
import org.bukkit.scheduler.BukkitRunnable;

public class TimerRunnable extends BukkitRunnable {
    TabuGame game;
    TabuPlayer player;
    int secs = 10;
    public TimerRunnable(TabuGame game, TabuPlayer player, String word) {
        this.game = game;
        this.player = player;
        game.sendMessageToAllPlayers(player.getName() + " ist an der Reihe");
        player.sendMessage(game.prefix() + "Du bist an der Reihe. Dein Wort lautet: " + ChatColor.YELLOW + word);

        this.run();
    }
    @Override
    public void run() {
        //debugging
        player.sendMessage(secs+"");


        switch (secs) {
            case 90:
                game.sendMessageToAllPlayers(game.prefix() + "Noch 1:30 Minuten");
                break;
            case 60:
                game.sendMessageToAllPlayers(game.prefix() + "Noch 1 Minute");
                break;
            case 30:
                game.sendMessageToAllPlayers(game.prefix() + "Noch 30 Sekunden");
                break;
            case 10:
                game.sendMessageToAllPlayers(game.prefix() + "Noch 10 Sekunden");
                break;
            case 3:
                game.sendMessageToAllPlayers(game.prefix() + "3");
                break;
            case 2:
                game.sendMessageToAllPlayers(game.prefix() + "2");
                break;
            case 1:
                game.sendMessageToAllPlayers(game.prefix() + "1");
                break;
        }

        if(secs <= 0) {
            this.stop();
        }
    }
    public void stop() {
        player.sendMessage("Vorbei");
        game.stopTimer();
    }
}
