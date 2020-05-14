package hdhotdog.adventuria.tabu;

import hdhotdog.adventuria.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TabuGame {
    public Main main;
    public Tabu tabuInstance;
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
    private Set<String> keys;
    public HashSet<String> words;
    public TabuTimer timer;
    private BukkitTask bukkitTask;
    private int currentRound = 1;
    //-------- Constructors --------------------------------------------------------------------------------------------
    public TabuGame(TabuPlayer creator, String name, int rounds) {
        this.creator = creator;
        this.name = name;
        this.rounds = rounds;
        this.gameID = games;
        games++;

        this.players = new Hashtable<>();
        this.bannedPlayers = new HashSet<String>();
        this.words = new HashSet<>();
    }
    public void setMain(Main m) {
        this.main = m;
    }
    public void setTabuInstance(Tabu tabu) {
        this.tabuInstance = tabu;
    }

    public TabuGame(TabuPlayer creator, String name) {
        this(creator, name, 3);
    }

    public TabuGame(TabuPlayer creator) {
        this(creator,"Tabu-Spiel" + games, 3);
    }

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
        //Set<String> keys = players.keySet();
        ArrayList<TabuPlayer> listToRemove = new ArrayList(players.values());
        for(TabuPlayer player : listToRemove) {
            kickPlayer(player.getName(), "Das Spiel ist vorbei!");
        }
        creator.sendMessage(prefix()+this.name + " wurde beendet");
    }

    //-------- addPlayer -----------------------------------------------------------------------------------------------
    public boolean addPlayer(String player) {
        if(running) {
            return false;
        }
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


    //-------- removePlayer --------------------------------------------------------------------------------------------
    public boolean removePlayer(String player) {
        boolean removed = null == players.remove(player);
        keys = players.keySet();
        return removed;
    }


    //-------- addWord -------------------------------------------------------------------------------------------------
    public boolean addWord(String word) {
        return this.words.add(word);
    }

    public boolean[] addWords(String[] words){
        boolean[] wordsAdded = new boolean[words.length-1];
        for (int i = 1; i < words.length; i++) {
            wordsAdded[i-1] = addWord(words[i]);
        }
        return wordsAdded;
    }

    //-------- removeWord ----------------------------------------------------------------------------------------------
    public boolean removeWord(String word) {
        return this.words.remove(word);
    }

    public boolean[] removeWords(String[] words){
        boolean[] wordsRemoved = new boolean[words.length-1];
        for (int i = 1; i < words.length; i++) {
            wordsRemoved[i-1] = removeWord(words[i]);
        }
        return wordsRemoved;
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
        if(!playerNotBanned) {
            this.players.get(player).sendMessage(prefix() + "Du wurdest entbannt.");
        }
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
        Objects.requireNonNull(Bukkit.getPlayer(player)).sendMessage(this.prefix() + message);
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
        this.running = true;
        ArrayList<TabuPlayer> listOfPlayers = new ArrayList<>(players.values());
        ArrayList<TabuPlayer> list = new ArrayList<>(this.players.values());
        roundRunning = true;
        currentPlayer = list.get(0);
        startThread(currentPlayer);

    }

    //-------- calculate all winners -----------------------------------------------------------------------------------
    private void calculateWinners(TabuPlayer playerObject) {
        if(winners.size() == 0){
            winners.add(playerObject);
        }else {
            if(winners.get(0).getPoints() < playerObject.getPoints()) {
                winners.clear();
                winners.add(playerObject);
            } else if(winners.get(0).getPoints() == playerObject.getPoints()) {
                winners.add(playerObject);
            }
        }
    }

    private void startThread(TabuPlayer player) {
        ArrayList<TabuPlayer> pls = new ArrayList<>(players.values());
        bukkitTask = Bukkit.getScheduler().runTask(this.main, new TabuTimer(this,player, pls));
    }
    public void stopThread() {
        bukkitTask.cancel();
        ArrayList<TabuPlayer> list = new ArrayList<>(this.players.values());
        if((rounds == currentRound && currentPlayer.equals(list.get(list.size()-1)))) {
            players.forEach((key,value) -> calculateWinners(value));
            announceWinners();
            this.quitGame();
            Bukkit.getScheduler().cancelTask(timer.taskID);
            Tabu.tabuGames.remove(this.getName());

        } else {
            if(currentPlayer.equals(list.get(list.size()-1))) {
                currentPlayer = list.get(0);
                currentRound++;
            } else {
                currentPlayer = list.get(list.indexOf(currentPlayer)+1);
            }
            startThread(currentPlayer);
        }

    }
    private void announceWinners() {
        String winnerMessage = "Gewinner: ";
        for (TabuPlayer winner : this.winners) {
            winnerMessage += winner.getName() + " ";
        }
        this.sendMessageToAllPlayers(winnerMessage);
    }

    public String getWords() {
        ArrayList<String> wordsInOrder = new ArrayList<>();
        wordsInOrder.addAll(words);
        Collections.sort(wordsInOrder);
        String s = "";
        for (String word : wordsInOrder) {
            s += word + ", ";
        }
        if(!s.equals("")) {
            s = s.substring(0, s.length()-2);
        }
        return s;
    }



    //-------- ChatEvent Handler -----------------------------------------------------------------------
    public void chatEvent(AsyncPlayerChatEvent e) {
        if(roundRunning && timer != null){
            timer.chatEvent(e);
        }
        Bukkit.getConsoleSender().sendMessage(timer.players.size() +"");
    }
}

