package hdhotdog.advi.plugins;

import org.bukkit.entity.Player;

import java.util.*;

public class TabuGame {
    private Hashtable<String, TabuPlayer> players;
    private ArrayList<String> wordList = new ArrayList<String>();
    private String name;
    private int rounds;
    private int gameID;
    private static int games = 0;
    private TabuPlayer creator;

    public TabuGame(String name, int rounds) {
        this.name = name;
        this.rounds = rounds;
        this.gameID = games;
        games++;

        this.players = new Hashtable<>();
    }

    public TabuGame(String name) {
        this(name, 3);
    }

    public TabuGame() {
        this("Tabu-Game" + games, 3);
    }

    public boolean addPlayer(Player player) {
        boolean playerAlreadyAdded = this.players.containsKey(player.getName());
        if (!playerAlreadyAdded) {
            players.put(player.getName(), new TabuPlayer(player));
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


}
