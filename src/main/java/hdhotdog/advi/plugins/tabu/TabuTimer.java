package hdhotdog.advi.plugins.tabu;

import com.google.gson.internal.$Gson$Preconditions;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.ArrayList;
import java.util.Random;

public class TabuTimer implements Runnable {
    private TabuGame game;
    private TabuPlayer player;
    private String word;
    private static int currentRound = 0;
    private static int currentPlayer = 0;
    private int roundLimit;
    private ArrayList<TabuPlayer> players;
    private ArrayList<String> words;
    private ArrayList<TabuPlayer> winners = new ArrayList<>();

    //private Thread thread;
    private boolean running = true;
    public TabuTimer(TabuGame game, int rounds, ArrayList<TabuPlayer> players) {
        this.game = game;
        this.words = new ArrayList<>(this.game.words);
        this.roundLimit = rounds;
        this.players = players;
        if(currentPlayer == players.size()) {
            currentRound++;
            currentPlayer = 0;
        } else {
            currentPlayer++;
        }
        Random random = new Random();
        String word = this.words.get(random.nextInt(this.words.size()));
        player = players.get(currentPlayer-1);
        game.sendMessageToAllPlayers(game.prefix() + player.getName() + " ist an der Reihe.");
        player.getPlayer().sendMessage(game.prefix() + "Du bist an der Reihe! Dein Wort lautet " + ChatColor.YELLOW + word);
    }

    public void run() {
        try {
            synchronized (this) {
                this.wait(30000);
                game.sendMessageToAllPlayers(game.prefix() + "Noch 90 Sekunden!");
                this.wait(30000);
                game.sendMessageToAllPlayers(game.prefix() + "Noch 60 Sekunden!");
                this.wait(30000);
                game.sendMessageToAllPlayers(game.prefix() + "Noch 30 Sekunden!");
                this.wait(20000);
                game.sendMessageToAllPlayers(game.prefix() + "Noch 10 Sekunden!");
                this.wait(7000);
                game.sendMessageToAllPlayers(game.prefix() + "3");
                this.wait(1000);
                game.sendMessageToAllPlayers(game.prefix() + "2");
                this.wait(1000);
                game.sendMessageToAllPlayers(game.prefix() + "1");
                this.wait(1000);
            }
            game.sendMessageToAllPlayers(game.prefix() + "Vorbei! Das Wort war " + ChatColor.YELLOW + word);
            if(currentRound == roundLimit && currentPlayer == players.size()) {
                int maxPoints = 0;
                for(int i = 0; i < players.size(); i++) {
                    if(players.get(i).getPoints() > maxPoints) {
                        winners.clear();
                        winners.add(players.get(i));
                        maxPoints = players.get(i).getPoints();
                    } else if(players.get(i).getPoints() >= maxPoints) {
                        winners.add(players.get(i));
                        maxPoints = players.get(i).getPoints();
                    }
                }
                String winnerMessage = "Gewinner: ";
                for(TabuPlayer winner: this.winners){
                    winnerMessage += winner.getName() + " ";
                }
                winnerMessage += String.format("mit %d Punkten", this.winners.get(0).getPoints());
                this.game.sendMessageToAllPlayers(game.prefix() + winnerMessage);
                this.game.quitGame();
            }
        } catch (InterruptedException e) {
            e.getStackTrace();
    }

    }

    public int getRound() {
        return currentRound;
    }
    public int getCurrentPlayer() {
        return currentPlayer;
    }
    public void chatEvent(AsyncPlayerChatEvent e) {
        if(e.getPlayer().equals(players.get(currentPlayer-1).getPlayer())) {
            if(!e.getMessage().startsWith("/tabu")) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(game.prefix()+"Du kannst keine Nachrichten senden, während du an der Reihe bist");
            }
        } else {
            ArrayList<Player> p = new ArrayList<>();
            for(TabuPlayer tp : players) {
                p.add(tp.getPlayer());
            }
            if(p.contains(e.getPlayer()) && e.getMessage().equalsIgnoreCase(word)) {
                game.sendMessageToAllPlayers(e.getPlayer().getName() + " hat den Begriff " + ChatColor.YELLOW + word + ChatColor.BLUE + "korrekt erraten!");
                TabuPlayer winner;
                for(TabuPlayer pl : players) {
                    if(pl.getPlayer().equals(e.getPlayer())) {
                        winner = pl;
                        winner.addPoint();
                        winner.getPlayer().sendMessage(game.prefix() + "Du hast einen Punkt erhalten. Aktuelle Punktzahl: " + winner.getPoints());
                        break;
                    }
                }


            }
        }
    }
}


//game.sendMessageToAllPlayers(player.getName() + " ist an der Reihe");
//player.sendMessage(game.prefix() + "Du bist an der Reihe. Dein Wort lautet: " + ChatColor.YELLOW + word);