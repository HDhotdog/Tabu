package hdhotdog.advi.plugins.tabu;

import com.google.common.collect.ArrayListMultimap;
import hdhotdog.advi.plugins.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TabuGame {
    public Main main;
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
    private static String prefix = ChatColor.BLUE + "[TABU] " + ChatColor.GREEN;
    private boolean roundRunning = false;
    private String currentWord;
    private Set<String> keys;
    public HashSet<String> words;
    public int taskID;
    public TabuTimer timer;
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
        this.words = new HashSet<>();
    }
    public void setMain(Main m) {
        main = m;
    }

    public TabuGame(TabuPlayer creator, String name) {
        this(creator, name, 3);
    }

    public TabuGame(TabuPlayer creator) {
        this( creator,"Tabu-Spiel" + games, 3);
    }

    public TabuGame(){}

    public Hashtable<String, TabuPlayer> getPlayers() {
        return this.players;
    }
    public TabuPlayer getCreator() {
        return this.creator;
    }
    public String getName() {
        return this.name;
    }
    public void quitGame() {
        players.forEach((key, value) -> kickPlayer(key, "Das Spiel ist vorbei."));
        creator.sendMessage(prefix()+this.name + " wurde beendet");
    }

    //-------- addPlayer -----------------------------------------------------------------------------------------------
    public boolean addPlayer(String player) {
        boolean playerAlreadyAdded = this.players.containsKey(player);
        Player newPlayer = Bukkit.getPlayer(player);
        if (!playerAlreadyAdded && newPlayer != null) {
            TabuPlayer tabuPlayer = new TabuPlayer(newPlayer);
            players.put(player, tabuPlayer);
            tabuPlayer.joinedGame(true);
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
        boolean removed = null == players.remove(player);
        keys = players.keySet();
        return removed;
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
            if(words[i] != null) {
                wordsAdded[i] = addWord(words[i]);
            }

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
    //-------- unbanPlayer -----------------------------------------------------------------------------------------------
    public boolean unbanPlayer(String player) {
        boolean playerNotBanned = !this.bannedPlayers.contains(player);
        bannedPlayers.remove(player);
        return playerNotBanned;
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
    public String prefix() {
        return ChatColor.BLUE + String.format("[%s] ", this.name) + ChatColor.GREEN;
    }

    private void sendMessage(String player, String message){
        Bukkit.getPlayer(player).sendMessage(this.prefix() + message);
    }

    public void sendMessageToAllPlayers(String message) {
        Enumeration<String> currentlyJoinedPlayers = this.players.keys();
        while (currentlyJoinedPlayers.hasMoreElements()) {
            sendMessage(currentlyJoinedPlayers.nextElement(), message);
        }
    }

    private boolean playerInGame(String player){
        return this.players.containsKey(player);
    }

    public String toString(){
        return String.format(prefix() + "%s hat %d Spieler und %d Wörter die über %d Runden geraten werden.",
                this.name, this.players.size(), this.words.size(), this.rounds);
    }

    //-------- game control --------------------------------------------------------------------------------------------
    public void start(){
        running = true;
        ArrayList<TabuPlayer> listOfPlayers = new ArrayList<>(players.values());
        taskID = Bukkit.getServer().getScheduler().scheduleSyncRepeatingTask(this.main, timer = new TabuTimer(this, rounds, listOfPlayers),0, 120*20L);

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
            startTimer(currentPlayer, this.currentWord);
        }
    }

    private void startTimer(TabuPlayer player, String word) {
        /*//taskID = player.getPlayer().getServer().getScheduler().scheduleSyncRepeatingTask(this.main, new TimerRunnable(this, player, word), 0, 20L);
        TabuTimer timer = new TabuTimer(this, player, word);
        timer.run();
        while(roundRunning) {
            try {
                this.wait(10);
            } catch (InterruptedException e) {
                e.printStackTrace();
            }
        }*/
        sendMessageToAllPlayers(player.getName() + " ist an der Reihe");
        player.sendMessage(prefix() + "Du bist an der Reihe. Dein Wort lautet: " + ChatColor.YELLOW + word);

    }
    public void stopTimer() {
        //Bukkit.getScheduler().cancelTask(taskID);
        roundRunning = false;
    }
    public void startRunning() {
        this.roundRunning = true;
    }
    public void endRound() {
        this.roundRunning = false;
    }
    public boolean isRunning() {
        return this.running;
    }

    //-------- keine Ahnung. Das hat Oci gemacht -----------------------------------------------------------------------
    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent e) {
        timer.chatEvent(e);
    }
}

