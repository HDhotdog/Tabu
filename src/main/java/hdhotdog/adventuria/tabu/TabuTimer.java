package hdhotdog.adventuria.tabu;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.entity.Player;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.scheduler.BukkitScheduler;
import java.util.ArrayList;
import java.util.Random;

public class TabuTimer implements Runnable {
    private TabuGame game;
    private TabuPlayer player;
    private String word;
    public ArrayList<TabuPlayer> players;
    private ArrayList<String> words;
    private ArrayList<TabuPlayer> winners = new ArrayList<>();
    public int taskID;
    public int loop = 0;

    public TabuTimer(TabuGame game, TabuPlayer player, ArrayList<TabuPlayer> players) {
        this.players = players;
        this.game = game;
        this.player = player;
        this.words = new ArrayList<>(this.game.words);

        Random random = new Random();
        word = this.words.get(random.nextInt(this.words.size()));
        game.sendMessageToAllPlayers(player.getName() + " ist an der Reihe.");
        player.getPlayer().sendMessage(game.prefix() + "Du bist an der Reihe! Dein Wort lautet " + ChatColor.YELLOW + word);
        game.timer = this;
    }
    @Override
    public void run() {
        loop = 0;
        BukkitScheduler scheduler = this.game.main.getServer().getScheduler();

         taskID = scheduler.scheduleSyncRepeatingTask(this.game.main, new Runnable() {
            @Override
            public void run() {
                if(120-(30*loop) != 0) {
                    game.sendMessageToAllPlayers("Noch " + (120 - (30 * loop)) + " Sekunden");
                }
                loop++;
                if(loop == 5) {
                    cancelTask();
                    game.sendMessageToAllPlayers("Vorbei! Das Wort war " + ChatColor.YELLOW + word);
                    game.stopThread();
                }
            }
        },0,20*5L);
    }
    public void cancelTask() {
        Bukkit.getServer().getScheduler().cancelTask(taskID);
    }

    public void chatEvent(AsyncPlayerChatEvent e) {

        if(e.getPlayer().equals(player.getPlayer())) {
            if(!e.getMessage().startsWith("/tabu")) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(game.prefix()+"Du kannst keine Nachrichten senden, während du an der Reihe bist");
            }
        } else {
            ArrayList<Player> p = new ArrayList<>();
            Bukkit.getConsoleSender().sendMessage(players.size() + "");
            for(TabuPlayer tp : players) {
                p.add(tp.getPlayer());
            }
            if(p.contains(e.getPlayer()) && e.getMessage().equalsIgnoreCase(word)) {
                game.sendMessageToAllPlayers(e.getPlayer().getName() + " hat den Begriff " + ChatColor.YELLOW + word + ChatColor.GREEN + " korrekt erraten!");
                this.cancelTask();
                TabuPlayer winner;
                for(TabuPlayer pl : players) {
                    if(pl.getPlayer().equals(e.getPlayer())) {
                        winner = pl;
                        winner.addPoint();
                        winner.getPlayer().sendMessage(game.prefix() + "Du hast einen Punkt erhalten. Aktuelle Punktzahl: " + winner.getPoints());
                        break;
                    }
                }
                game.stopThread();

            }
        }
    }
}
