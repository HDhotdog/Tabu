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

        this.players = new Hashtable<>();
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
        if (!playerAlreadyAdded) {
            players.put(player, new TabuPlayer(Bukkit.getPlayer(player)));
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

    public void tellRemainingTime(String time) {
        sendMessageToAllPlayers(this.prefix() + "Noch " + time + "!");
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

