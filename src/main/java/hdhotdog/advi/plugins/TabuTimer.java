package hdhotdog.advi.plugins;

import org.bukkit.ChatColor;

import javax.swing.*;

public class TabuTimer implements Runnable {
    private TabuGame game;
    private TabuPlayer player;
    private String word;
    //private Thread thread;
    private boolean running = true;
    public TabuTimer(TabuGame game, TabuPlayer player, String word) {
        this.game = game;
        this.player = player;
        this.word = word;
        //this.thread = new Thread(this);
    }

    public void run() {
        game.sendMessageToAllPlayers(player.getName() + " ist an der Reihe");
        player.sendMessage(game.prefix() + "Du bist an der Reihe. Dein Wort lautet: " + ChatColor.YELLOW + word);
        this.game.startRunning();
        for(int i = 1; i <= 4; i++) {
            boolean overtime = false;
            long time = System.currentTimeMillis();
            while(!overtime) {
                 long time2 = System.currentTimeMillis();
                 if (time2-time > 30000) {
                     overtime = true;
                 }
            }
            game.sendMessageToAllPlayers(game.prefix() + (4-i)*30 + " verbleibend!");
        }
        this.game.endRound();
    }

    /*public void start() {
        this.game.startRunning();
        this.running = true;
        if(running) {
            this.thread.start();
        }
    }*/
    /*public void run() {
        try {
            this.wait(30000);
            game.sendMessageToAllPlayers(game.prefix() + "Noch 90 Sekunden");
            this.wait(30000);
            game.sendMessageToAllPlayers(game.prefix() +"Noch 60 Sekunden");
            this.wait(30000);
            game.sendMessageToAllPlayers(game.prefix() +"Noch 30 Sekunden");
            this.wait(20000);
            game.sendMessageToAllPlayers(game.prefix() +"Noch 10 Sekunden");
            this.wait(7000);
            game.sendMessageToAllPlayers(game.prefix() +"3");
            this.wait(1000);
            game.sendMessageToAllPlayers(game.prefix() +"2");
            this.wait(1000);
            game.sendMessageToAllPlayers(game.prefix() +"1");
            this.wait(1000);
            game.sendMessageToAllPlayers(game.prefix() +"Ende!");
            this.stop();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void stop() {
        this.running = false;
        this.thread.stop();
        this.game.endRound();
    }*/
}
