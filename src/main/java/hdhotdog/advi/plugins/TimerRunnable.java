package hdhotdog.advi.plugins;

import org.bukkit.ChatColor;

public class TimerRunnable implements Runnable {
    TabuGame game;
    int sec = 120;
    public TimerRunnable(TabuGame game, TabuPlayer player, String word) {
        this.game = game;
        game.sendMessageToAllPlayers(player.getName() + " ist an der Reihe");
        player.sendMessage(game.prefix() + "Du bist an der Reihe. Dein Wort lautet: " + ChatColor.YELLOW + word);
        this.run();
    }
    public void run() {
        switch (sec) {
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
        sec--;
        if(sec == 0) {
            this.stop();
        }
    }
    public void stop() {
        game.stopTimer();
    }
}
