package hdhotdog.adventuria.tabu;

import hdhotdog.adventuria.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import org.bukkit.scheduler.BukkitTask;

import java.util.*;

public class TabuGame {
    public Main main;
    public Tabu tabuInstance;
    public boolean eventGame;
    private final Hashtable<String, TabuPlayer> players;
    private final HashSet<String> bannedPlayers;
    private final ArrayList<TabuPlayer> winners = new ArrayList<>();
    private final String name;
    private final int rounds;
    private final int gameID;
    private static int games = 0;
    private final TabuPlayer creator;
    private boolean running = false;
    private TabuPlayer currentPlayer;
    private boolean roundRunning = false;
    private Set<String> keys;
    public HashSet<String> words;
    public static HashSet<String> basicWords = new HashSet<>();
    public TabuTimer timer;
    private BukkitTask bukkitTask;
    public String eventWarp;
    public static String basicWordsString = "Apfel Haus Baum Kirche Auto Fisch Maus Kuh Computer Lampe";
    private final LinkedList<TabuPlayer> playerQueue = new LinkedList<>();
    //-------- Constructors --------------------------------------------------------------------------------------------
    public TabuGame(TabuPlayer creator, String name, int rounds) {
        this.creator = creator;
        this.name = name;
        this.rounds = rounds;
        this.gameID = games;
        games++;

        this.players = new Hashtable<>();
        this.bannedPlayers = new HashSet<>();
        this.words = new HashSet<>();
        words.addAll(Arrays.asList(basicWordsString.split(" ")));
    }
    public void setMain(Main m) {
        this.main = m;
    }
    public void setTabuInstance(Tabu tabu) {
        this.tabuInstance = tabu;
    }
    public void setWarp(String warp) {
        this.eventWarp = warp;
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
        ArrayList<TabuPlayer> listToRemove = new ArrayList<>(players.values());
        for(TabuPlayer player : listToRemove) {
            kickPlayer(player.getName(), "Das Spiel ist vorbei!");
        }
        creator.sendMessage(prefix()+this.name + " wurde beendet");
        if(running) {
            this.timer.cancelTask();
        }
        Tabu.tabuGames.remove(this.name);
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
    public void removePlayer(String player) {
        players.remove(player);
        keys = players.keySet();
        if(players.isEmpty()) {
            this.quitGame();

            bukkitTask.cancel();
        }
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
    public void kickPlayer(String player, String message) {
        boolean playerInGame = this.players.containsKey(player);
        if(playerInGame){
            this.players.get(player).sendMessage(this.prefix() + message);
            this.players.remove(player);
        }
    }

    public void kickPlayer(String player){
        kickPlayer(player, "Du wurdest gekickt.");
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
        ArrayList<TabuPlayer> list = new ArrayList<>(this.players.values());
        for(int i = 0; i < this.rounds; i++) {
            playerQueue.addAll(list);
        }
        roundRunning = true;
        currentPlayer = playerQueue.removeFirst();
        while(!players.contains(currentPlayer)) {
            currentPlayer = playerQueue.removeFirst();
        }
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
        if(this.eventGame) {
            Bukkit.dispatchCommand(player.getPlayer(), "warp " + this.eventWarp);
        }
    }
    public void stopThread() {
        bukkitTask.cancel();

        if(playerQueue.isEmpty()) {
            players.forEach((key,value) -> calculateWinners(value));
            announceWinners();
            Bukkit.getScheduler().cancelTask(timer.taskID);
            running = false;
            players.forEach((key,value) -> value.clearPoints());
            creator.sendMessage(prefix() + "Das Spiel ist vorbei. Nutze /tabu play um eine neue Runde zu starten.");
            winners.clear();
            /*this.quitGame();
            Bukkit.getScheduler().cancelTask(timer.taskID);
            Tabu.tabuGames.remove(this.getName());*/

        } else {
            currentPlayer = playerQueue.removeFirst();
            while (!players.contains(currentPlayer)) {
                currentPlayer = playerQueue.removeFirst();
            }
            startThread(currentPlayer);
        }

    }
    private void announceWinners() {
        StringBuilder winnerMessage = new StringBuilder("Gewinner: ");
        for (TabuPlayer winner : this.winners) {
            winnerMessage.append(winner.getName()).append(" ");
        }
        this.sendMessageToAllPlayers(winnerMessage.toString());
    }

    public String getWords() {
        ArrayList<String> wordsInOrder = new ArrayList<>();
        wordsInOrder.addAll(words);
        Collections.sort(wordsInOrder);
        StringBuilder s = new StringBuilder();
        for (String word : wordsInOrder) {
            s.append(word).append(", ");
        }
        if(!s.toString().equals("")) {
            s = new StringBuilder(s.substring(0, s.length() - 2));
        }
        return s.toString();
    }



    //-------- ChatEvent Handler -----------------------------------------------------------------------
    public void chatEvent(AsyncPlayerChatEvent e) {
        if(roundRunning && timer != null){
            timer.chatEvent(e);
        }
    }

    public void quitEvent(PlayerQuitEvent e) {
        Player player = e.getPlayer();
        this.leaveGame(player.getName());
    }
}

