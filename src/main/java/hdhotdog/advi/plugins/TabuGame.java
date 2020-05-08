package hdhotdog.advi.plugins;

import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class TabuGame {
    private Hashtable<String, TabuPlayer> players;
    private ArrayList<String> wordList = new ArrayList<String>();
    private ArrayList<TabuPlayer> winners = new ArrayList<>();
    private String name;
    private int rounds;
    private int gameID;
    private static int games = 0;
    private TabuPlayer creator;
    private boolean running = false;
    private TabuPlayer currentPlayer;
    private static String prefix = ChatColor.BLUE + "[TABU] ";
    private boolean roundRunning = false;
    private String currentWord;
    private TabuTimer timer;
    private Set<String> keys;


    public TabuGame(TabuPlayer creator, String name, int rounds) {
        this.creator = creator;
        this.name = name;
        this.rounds = rounds;
        this.gameID = games;
        games++;

        this.players = new Hashtable<>();
    }

    public TabuGame(TabuPlayer creator, String name) {
        this(creator, name, 3);
    }

    public TabuGame(TabuPlayer creator) {
        this(creator,"Tabu-Spiel" + games, 3);
    }

    public boolean addPlayer(Player player) {
        boolean playerAlreadyAdded = this.players.containsKey(player.getName());
        if (!playerAlreadyAdded) {
            players.put(player.getName(), new TabuPlayer(player));
            keys = players.keySet();
            return true;
        }
        return false;
    }

    public boolean[] addPlayers(Player[] players){
        boolean[] playersAdded = new boolean[players.length];
        for (int i = 0; i < players.length; i++) {
            playersAdded[i] = addPlayer(players[i]);
        }
        return playersAdded;
    }

    public boolean removePlayer(Player player) {
        keys = players.keySet();
        return null == players.remove(player.getName());
    }

    public boolean[] removePlayers(Player[] players){
        boolean[] playersRemoved = new boolean[players.length];
        for (int i = 0; i < players.length; i++) {
            playersRemoved[i] = removePlayer(players[i]);
        }
        return playersRemoved;
    }
    public boolean addWord(String word) {
        for(String w : wordList) {
            if(w.equalsIgnoreCase(word)) {
                return false;
            }
        }
        return wordList.add(word);
    }
    public boolean removeWord(String word) {
        for(String w : wordList) {
            if(w.equalsIgnoreCase(word)) {
                wordList.remove(word);
                return true;
            }
        }
        return false;
    }

    public void start() {
        running = true;
        for(int i = 0; i < rounds; i++) {
            Random random = new Random();
            Set<String> keys = players.keySet();
            for(String key : keys) {
                String word = wordList.get(random.nextInt(wordList.size()));
                currentWord = word;
                currentPlayer = players.get(key);
                if(currentPlayer != null) {
                    choosePlayer(currentPlayer, word);
                    while (roundRunning) {

                    }
                }
            }
        }
        int winnerPoints = 0;
        TabuPlayer winner;
        for(String key : keys) {
            int points = players.get(key).getPoints();
            if(points == winnerPoints) {
                winners.add(players.get(key));
            } else if(points > winnerPoints) {
                winners.clear();
                winners.add(players.get(key));
            }
        }
        if(winners.size() == 1) {
            winner = winners.get(0);
            sendMessageToAllPlayers(winner.getName() + " hat das Spiel mit " + winnerPoints + " Punkten gewonnen!");
        } else {
            StringBuilder sb = new StringBuilder();
            for(TabuPlayer player: winners) {
                sb.append(player.getName()).append(", ");
            }
            String query = sb.substring(0,sb.length()-2);
            sendMessageToAllPlayers(query + " haben das Spiel mit jeweils " + winnerPoints + " Punkten gewonnen!");
        }

        Tabu.quitGame(this);

    }
    private void stop() {
        running = false;
    }
    private void choosePlayer(TabuPlayer player, String word) {
        sendMessageToAllPlayers(player.getName() + " ist an der Reihe");
        player.getPlayer().sendMessage(prefix + "Du bist an der Reihe. Dein Wort lautet: " + ChatColor.YELLOW + word);
        timer = new TabuTimer(this);
    }

    public void sendMessageToAllPlayers(String message) {
        Set<String> keys = players.keySet();
        for(String key : keys) {
            players.get(key).getPlayer().sendMessage(prefix + message);
        }
    }
    public void tellRemainingTime(String time) {
        sendMessageToAllPlayers(prefix + "Noch " + time + "!");
    }
    public void endRound() {
        this.roundRunning = false;
    }

    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent e) {
        if(e.getPlayer().equals(currentPlayer.getPlayer())) {
            if(!e.getMessage().startsWith("/tabu")) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(prefix+"Du kannst keine Nachrichten senden, während du an der Reihe bist");
            }
        } else {
            if(players.containsKey(e.getPlayer().getName()) && e.getMessage().equalsIgnoreCase(currentWord)) {
                timer.stop();
                sendMessageToAllPlayers(e.getPlayer().getName() + " hat den Begriff " + ChatColor.YELLOW + currentWord + ChatColor.BLUE + "korrekt erraten!");
                TabuPlayer winner = players.get(e.getPlayer().getName());
                winner.addPoint();
                winner.getPlayer().sendMessage(prefix + "Du hast einen Punkt erhalten. Aktuelle Punktzahl: " + winner.getPoints());
            }
        }
    }
}
