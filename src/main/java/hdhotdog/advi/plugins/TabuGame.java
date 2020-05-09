package hdhotdog.advi.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.util.*;

public class TabuGame {
    private Hashtable<String, TabuPlayer> players;
    private HashSet<String> bannedPlayers;
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
    private HashSet<String> words;

    //-------- Constructors --------------------------------------------------------------------------------------------
    public TabuGame(TabuPlayer creator, String name, int rounds) {
        this.creator = creator;
        this.name = name;
        this.rounds = rounds;
        this.gameID = games;
        games++;

        this.winners.add(creator);
        this.players = new Hashtable<>();
        this.bannedPlayers = new HashSet<String>();
    }

    public TabuGame(TabuPlayer creator, String name) {
        this(creator, name, 3);
    }

    public TabuGame(TabuPlayer creator) {
        this(creator,"Tabu-Spiel" + games, 3);
    }

    //-------- addPlayer -----------------------------------------------------------------------------------------------
    public boolean addPlayer(String player) {
        boolean playerAlreadyAdded = this.players.containsKey(player);
        Player newPlayer = Bukkit.getPlayer(player);
        if (!playerAlreadyAdded && newPlayer != null) {
            players.put(player, new TabuPlayer(newPlayer));
            keys = players.keySet();
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
        keys = players.keySet();
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
                return;
            }
            sendMessage(player, "Du bist dem Spiel schon beigetreten!");
            return;
        }
        sendMessage(player, "Du kannst dem Spiel nicht beitreten, da du gebannt wurdest!");
    }

    //-------- leaveGame -----------------------------------------------------------------------------------------------
    public void leaveGame(String player) {
        if(playerInGame(player)){
            removePlayer(player);
            sendMessage(player, "Du hast das Spiel verlassen.");
            sendMessageToAllPlayers(player + " hat das Spiel verlassen.");
            return;
        }
        sendMessage(player, "Du bist in keinem Spiel");
    }


    //-------- misc ----------------------------------------------------------------------------------------------------
    private String prefix() {
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

    //-------- game control --------------------------------------------------------------------------------------------
    public void start(){
        running = true;
        for(;this.rounds > 0; rounds--){
            players.forEach((playerName, playerObject)-> startRoundFor(playerName));
        }

        this.players.forEach((playerName, playerObject)-> calculateWinners(playerObject));
        String winnerMessage = "Gewinner: ";
        for(TabuPlayer winner: this.winners){
            winnerMessage += winner.getName() + " ";
        }
        winnerMessage += String.format("mit %d Punkten", this.winners.get(0).getPoints());
        Tabu.quitGame(this);
    }

    //-------- calculate all winners -----------------------------------------------------------------------------------
    private void calculateWinners(TabuPlayer playerObject) {
        if(winners.get(0).getPoints() < playerObject.getPoints()){
            winners.clear();
            winners.add(playerObject);
        }else if(winners.get(0).getPoints() == playerObject.getPoints()){
            winners.add(playerObject);
        }
    }

    //-------- starts a round for the chosen player --------------------------------------------------------------------
    private void startRoundFor(String player){
        Random randy = new Random();
        String[] wordArr = this.words.toArray(new String[words.size()]);
        this.currentWord = wordArr[randy.nextInt(words.size())];
        currentPlayer = players.get(player);
        if(currentPlayer != null) {
            sendMessageToAllPlayers("neue Runde startet");
            choosePlayer(currentPlayer, this.currentWord);
            while (roundRunning) {

            }
        }
    }

    private void choosePlayer(TabuPlayer player, String word) {
        sendMessageToAllPlayers(player.getName() + " ist an der Reihe");
        player.sendMessage(prefix + "Du bist an der Reihe. Dein Wort lautet: " + ChatColor.YELLOW + word);
        timer = new TabuTimer(this);
    }

    public void tellRemainingTime(String time) {
        sendMessageToAllPlayers(this.prefix() + "Noch " + time + "!");
    }

    public void endRound() {
        this.roundRunning = false;
    }

    //-------- keine Ahnung. Das hat Oci gemacht -----------------------------------------------------------------------
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

