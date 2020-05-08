package hdhotdog.advi.plugins;

import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.configuration.file.FileConfiguration;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;

import java.io.*;
import java.util.*;


public class Tabu implements CommandExecutor, Listener {

    private static ArrayList<TabuGame> tabuGames = new ArrayList<TabuGame>();


    private static FileConfiguration wordConfig;
    public static LinkedList<TabuPlayer> playerList = new LinkedList<>();
    public static ArrayList<Player> bannedPlayers = new ArrayList<Player>();
    public static ArrayList<String> wordList = new ArrayList<>();
    public static boolean running = false;
    public static boolean isStarted = false;
    public static String prefix = ChatColor.BLUE + "[TABU] ";
    public static String currentWord;
    public static TabuPlayer currentPlayer;
    public static String path = "words.txt";
    public static Main main;
    public static Player creator;

    public Tabu(FileConfiguration fileConfiguration, Main m) {
        wordConfig = fileConfiguration;
        main = m;
    }
    public Tabu() {

    }

    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        /**
         * Spiel erstellen
         */
        if(args[0].equalsIgnoreCase("create")) {
            String gameName = "";
            boolean hasName = false;
            int rounds = 0;
            boolean customRounds = false;
            TabuPlayer creator = null;
            boolean fromPlayer = false;
            if(args.length >= 2) {
                gameName = args[1];
                hasName = true;
            }
            if(args.length > 2) {
                try {
                    if (Integer.parseInt(args[2]) < 1) {
                        throw new IllegalArgumentException();
                    } else {
                        rounds = Integer.parseInt(args[1]);
                        customRounds = true;
                    }
                } catch (NumberFormatException e) {
                    sender.sendMessage(prefix + "Benutze /tabu create <Name> <Rundenanzahl>");
                    return true;
                } catch (IllegalArgumentException e) {
                    sender.sendMessage(prefix + "Rundenanzahl muss größer als 0 sein");
                    return true;
                }
            }

            if(sender instanceof Player) {
                creator = new TabuPlayer((Player)sender);
                fromPlayer = true;
            }

            TabuGame tabuGame;
            if(hasName && customRounds) {
                 tabuGame = new TabuGame(creator, gameName, rounds);
            } else if (hasName) {
                 tabuGame = new TabuGame(creator, gameName);
            } else {
                 tabuGame = new TabuGame(creator);
            }
            tabuGames.add(tabuGame);

            String creatorName = "Console";
            if(fromPlayer) {
                creatorName = creator.getName();
            }
            sentToAllOnlinePlayer(ChatColor.GREEN + creatorName +" hat ein Tabu-Spiel mit " + rounds + "Runden gestartet!");

            return true;
        }
        /**
         * Spiel beenden
         */
        else if(args.length == 1 && args[0].equalsIgnoreCase("quit")) {

        }
        /**
         * Wort hinzufügen
         */
        else if(args.length == 2 && args[0].equalsIgnoreCase("add")) {
            if(addWord(args[1])) {
                sender.sendMessage(prefix + args[1] + " wurde hinzugefügt.");
            } else {
                sender.sendMessage(prefix + args[1] + " ist schon vorhanden.");
            }
            return true;
        }
        /**
         * Wort entfernen
         */
        else if (args.length == 2 && args[0].equalsIgnoreCase("remove")) {
            if(deleteWord(args[1])) {
                sender.sendMessage(ChatColor.GREEN + args[1] + " wurde gelöscht.");
            } else {
                sender.sendMessage(ChatColor.RED + args[1] + " ist nicht vorhanden.");
            }
            return true;
        }
        /**
         * Spiel beitreten
         */
        else if (args.length == 1 && args[0].equalsIgnoreCase("join") && running) {
            if(sender instanceof Player) {
                TabuPlayer player = new TabuPlayer((Player)sender);
                joinGame(player);
            } else {
                sender.sendMessage(prefix+"Du kannst diesen Befehl hier nicht ausführen");
            }
        }
        /**
         * Spiel verlassen
         */
        else if (args.length == 1 && args[0].equalsIgnoreCase("leave") && running) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                for(TabuPlayer p : playerList) {
                    if (p.getPlayer().equals(player)) {
                        leaveGame(p);
                        break;
                    }
                }
            } else {
                sender.sendMessage(prefix+"Du kannst diesen Befehl hier nicht ausführen.");
            }
        }
        /**
         * Spiel starten
         */
        else if(args.length == 1 && args[0].equalsIgnoreCase("start") && running) {
            if(isStarted) {
                sender.sendMessage(prefix + "Spiel wurde schon gestartet.");
            } else {
                isStarted = true;

            }
        }

        /**
         * Spieler aus laufender Runde kicken
         */
        else if (args.length == 2 && args[0].equalsIgnoreCase("kick") && running) {
            if(kickPlayer(args[1])) {
                sender.sendMessage( prefix + "Spieler entfernt");
            } else {
                sender.sendMessage( prefix + "Spieler befindet sich nicht im Spiel");
            }
        }

        /**
         * Spieler aus laufender Runde bannen
         */
        else if(args.length == 2 && args[0].equalsIgnoreCase("ban") && running) {
            if(banPlayer(args[1])) {
                if(sender instanceof Player) {
                    for(TabuPlayer p : playerList) {
                        p.getPlayer().sendMessage(prefix + args[1] + " wurde aus dem Spiel gebannt");
                    }
                } else {
                    sender.sendMessage(prefix + args[1] + " wurde aus dem Spiel gebannt");
                    for(TabuPlayer p : playerList) {
                        p.getPlayer().sendMessage(prefix + args[1] + " wurde aus dem Spiel gebannt");
                    }
                }
            } else {
                sender.sendMessage(prefix + args[1] + " befindet sich nicht im Spiel");
            }

        }

        /**
         * Send List of Words to Sender
         */
        else if(args.length == 1 && args[0].equalsIgnoreCase("words")) {
            sender.sendMessage(prefix + getListOfWords());
        }
        return true;
    }

    public static boolean addWord(String word) {

        if(!wordList.contains(word)) {
            return wordList.add(word);
        }
        return false;

    }

    public static boolean deleteWord(String word) {
        boolean removed = false;
        for(String s : wordList) {
            if(s.equalsIgnoreCase(word)) {
                removed = wordList.remove(word);
            }
        }
        return removed;
    }

    public static boolean kickPlayer(String player) {
        for(TabuPlayer p : playerList) {
            if(p.getName().equalsIgnoreCase(player)) {
                playerList.remove(p);
                p.getPlayer().sendMessage(prefix + "Du wurdest entfernt");
                return true;
            }
        }
        return false;
    }
    public static boolean banPlayer(String player) {
        for(TabuPlayer p : playerList) {
            if(p.getName().equalsIgnoreCase(player)) {
                playerList.remove(p);
                bannedPlayers.add(p.getPlayer());
                p.getPlayer().sendMessage( prefix + "Du wurdest gebannt");
                return true;
            }
        }
        return false;
    }
    public static void joinGame(TabuPlayer player) {
        if(bannedPlayers.contains(player.getPlayer())) {
            player.getPlayer().sendMessage(prefix + "Du kannst dem Spiel nicht beitreten, da du gebannt wurdest!");
        } else {
            if(playerListcontainsPlayer(player.getPlayer())) {
                player.getPlayer().sendMessage(prefix +"Du bist dem Spiel schon beigetreten");
            } else {
                player.getPlayer().sendMessage( prefix + "Spiel beigetreten!");
                for(TabuPlayer p : playerList) {
                    p.getPlayer().sendMessage(prefix + player.getName() + " hat das Spiel betreten");
                }
                playerList.add(player);
            }
        }
    }

    public static void leaveGame(TabuPlayer player) {
        if(playerList.contains(player)) {
            player.getPlayer().sendMessage(prefix + "Du bist in keinem Spiel");
        } else {
            playerList.remove(player);
            player.getPlayer().sendMessage(prefix + "Du hast das Spiel verlassen");
            for(TabuPlayer p : playerList) {
                p.getPlayer().sendMessage(prefix + player.getName() + " hat das Spiel verlassen");
            }
        }
    }

    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent e) {
        if(e.getPlayer().equals(currentPlayer.getPlayer())) {
            if(!e.getMessage().startsWith("/")) {
                e.setCancelled(true);
                e.getPlayer().sendMessage(prefix+"Du kannst keine Nachrichten senden, während du an der Reihe bist");
            }
        } else {
            if(playerListcontainsPlayer(e.getPlayer()) && e.getMessage().equalsIgnoreCase(currentWord)) {
                sendMessageToAllPlayers(e.getPlayer().getName() + " hat den Begriff " + ChatColor.YELLOW + currentWord + ChatColor.BLUE + "korrekt erraten!");
            }
        }
    }

    public void sendMessageToAllPlayers(String message) {
        for(TabuPlayer p : playerList) {
            p.getPlayer().sendMessage(prefix + message);
        }
    }

    public static boolean playerListcontainsPlayer(Player player) {
        for(TabuPlayer p : playerList) {
            if(p.getPlayer().equals(player)) {
                return true;
            }
        }
        return false;
    }

    public String getListOfWords() {
        StringBuilder sb = new StringBuilder();
        sb.append(" ");
        for(int i = 0; i < wordList.size(); i++) {
            sb.append(wordList.get(i)).append(" ");
        }
        return sb.toString().trim();
    }

    public void sentToAllOnlinePlayer(String message) {
        for(Player p : Bukkit.getOnlinePlayers()) {
            p.sendMessage(prefix + message);
        }
    }

}
