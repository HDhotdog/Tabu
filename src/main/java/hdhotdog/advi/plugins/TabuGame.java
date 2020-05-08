package hdhotdog.advi.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;

import java.util.*;

public class TabuGame {
    private Hashtable<String, TabuPlayer> players;
    private HashSet<String> bannedPlayers;
    private String name;
    private int rounds;
    private int gameID;
    private static int games = 0;
    private TabuPlayer creator;
    private HashSet<String> words;

    //-------- Constructors --------------------------------------------------------------------------------------------
    public TabuGame(String name, int rounds) {
        this.name = name;
        this.rounds = rounds;
        this.gameID = games;
        games++;

        this.words = new HashSet<String>();
        this.players = new Hashtable<>();
    }

    public TabuGame(String name) {
        this(name, 3);
    }

    public TabuGame() {
        this("Tabu-Game" + games, 3);
    }

    //-------- addPlayer -----------------------------------------------------------------------------------------------
    public boolean addPlayer(String player) {
        boolean playerAlreadyAdded = this.players.containsKey(player);
        if (!playerAlreadyAdded) {
            players.put(player, new TabuPlayer(Bukkit.getPlayer(player)));
            return true;
        }
        return false;
    }

    public boolean[] addPlayers(String[] players){
        boolean[] playersAdded = new boolean[players.length];
        for (int i = 0; i < players.length; i++) {
            playersAdded[i] = addPlayer(players[i]);
        }
        return playersAdded;
    }

    //-------- removePlayer --------------------------------------------------------------------------------------------
    public boolean removePlayer(String player) {
        return null == players.remove(player);
    }

    public boolean[] removePlayers(String[] players){
        boolean[] playersRemoved = new boolean[players.length];
        for (int i = 0; i < players.length; i++) {
            playersRemoved[i] = removePlayer(players[i]);
        }
        return playersRemoved;
    }

    //-------- addWord -------------------------------------------------------------------------------------------------
    public boolean addWord(String word) {
        return this.words.add(word);
    }
    
    public boolean[] addWords(String[] words){
        boolean[] wordsAdded = new boolean[words.length];
        for (int i = 0; i < words.length; i++) {
            wordsAdded[i] = addWord(words[i]);
        }
        return wordsAdded;
    }

    //-------- removeWord ----------------------------------------------------------------------------------------------
    public boolean removeWord(String word) {
        return this.words.remove(word);
    }

    public boolean[] removeWords(String[] words){
        boolean[] wordsAdded = new boolean[words.length];
        for (int i = 0; i < words.length; i++) {
            wordsAdded[i] = removeWord(words[i]);
        }
        return wordsAdded;
    }


    //-------- kickPlayer ----------------------------------------------------------------------------------------------
    public boolean kickPlayer(String player, String message) {
        boolean playerInGame = this.players.containsKey(player);
        if(playerInGame){
            this.players.get(player).sendMessage(this.prefix() + message);
            this.players.remove(player);
            return true;
        }
        return false;
    }

    public boolean kickPlayer(String player){
        return kickPlayer(player, "Du wurdest gekickt.");
    }

    //-------- banPlayer -----------------------------------------------------------------------------------------------
    public boolean banPlayer(String player) {
        boolean playerAlreadyBanned = this.bannedPlayers.contains(player);
        kickPlayer(player, "Du wurdest gebannt!");

        if(!playerAlreadyBanned){
            this.bannedPlayers.add(player);
            return true;
        }
        return false;
    }

    //-------- joinGame ------------------------------------------------------------------------------------------------
    public void joinGame(String player) {
        if(!this.bannedPlayers.contains(player)){
            if(addPlayer(player)){
                sendMessage(player, "Spiel beigetreten!");
                sendMessageToAllPlayers(player + " ist dem Spiel beigetreten.");
            }
            else{
                sendMessage(player, "Du bist dem Spiel schon beigetreten!");
            }
        }
        sendMessage(player, "Du kannst dem Spiel nicht beitreten, da du gebannt wurdest!");
    }

    //-------- leaveGame -----------------------------------------------------------------------------------------------
    public void leaveGame(String player) {
        if(playerInGame(player)){
            removePlayer(player);
            sendMessage(player, "Du hast das Spiel verlassen.");
            sendMessageToAllPlayers(player + " hat das Spiel verlassen.");
        }
        sendMessage(player, "Du bist in keinem Spiel");
    }


    //-------- misc ----------------------------------------------------------------------------------------------------
    private Object prefix() {
        return ChatColor.BLUE + String.format("[%s] ", this.name);
    }

    private void sendMessage(String player, String message){
        Bukkit.getPlayer(player).sendMessage(this.prefix() + message);
    }

    private void sendMessageToAllPlayers(String message) {
        Enumeration<String> currentlyJoinedPlayers = this.players.keys();
        while (currentlyJoinedPlayers.hasMoreElements()) {
            sendMessage(currentlyJoinedPlayers.nextElement(), message);
        }
    }

    private boolean playerInGame(String player){
        return this.players.containsKey(player);
    }

    public String toString(){
        return String.format("%s hat %d Spieler und %d Wörter die über %d Runden geraten werden.",
                this.name, this.players.size(), this.words.size(), this.rounds);
    }
}
