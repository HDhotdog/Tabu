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

    private static Hashtable<String, TabuGame> tabuGames = new Hashtable<>();
    public static ArrayList<String> wordList = new ArrayList<>();
    public static String prefix = ChatColor.BLUE + "[TABU] ";
    public static String path = "words.txt";
    public static Main main;


    public Tabu(FileConfiguration fileConfiguration, Main m) {
    }
    public Tabu(Main m) {
        main = m;
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
                    sender.sendMessage(args[2].trim());
                    if (Integer.parseInt(args[2]) < 1) {
                        throw new IllegalArgumentException();
                    } else {
                        rounds = Integer.parseInt(args[2]);
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
            tabuGame.setMain(main);
            if(tabuGames.containsKey(tabuGame.getName())) {
                sender.sendMessage(prefix + "Es existiert bereit ein Spiel mit diesen Namen.");
                return true;
            }
            tabuGames.put(tabuGame.getName(), tabuGame);

            String creatorName = "Console";
            if(fromPlayer) {
                creatorName = creator.getName();
            }
            if(rounds == 0) {
                rounds = 3;
            }
            sentToAllOnlinePlayer(ChatColor.GREEN + creatorName +" hat " + gameName + " mit " + rounds + " Runden gestartet!");

            return true;
        }
        /**
         * Spiele anzeigen
         */
        else if(args.length == 1 && args[0].equalsIgnoreCase("list")) {
            sender.sendMessage(prefix + "Aktuelle Spiele:");
            tabuGames.forEach((keys, values) -> sender.sendMessage(ChatColor.GREEN + "* "+ values.getName()));
        }
        /**
         * Spiel beenden
         */
        else if(args.length == 1 && args[0].equalsIgnoreCase("quit")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                Set<String> keys = tabuGames.keySet();
                for(String key : keys) {
                    if(tabuGames.get(key).getCreator().getPlayer().equals(player)) {
                        tabuGames.get(key).quitGame();
                        tabuGames.remove(key);
                        return true;
                    }
                }
                player.sendMessage(prefix + "Du hast keine offenen Spiele.");
            }
        }
        /**
         * Wort hinzufügen
         */
        else if(args.length >= 2 && args[0].equalsIgnoreCase("add")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                TabuGame game = getGameOfPlayer(player);
                if(game != null) {
                    args[0] = null;
                    boolean[] results = game.addWords(args);
                    for(int i = 0; i < results.length-1; i++) {
                        if(results[i]) {
                            player.sendMessage(prefix + args[i+1] + "wurde hinzugefügt");
                        } else {
                            player.sendMessage(prefix + args[i+1] + "existiert schon");
                        }
                    }
                }
                return true;
            }
            return true;
        }
        /**
         * Wort entfernen
         */
        else if (args.length >= 2 && args[0].equalsIgnoreCase("remove")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                TabuGame game = getGameOfPlayer(player);
                if(game != null) {
                    args[0] = null;
                    boolean[] results = game.removeWords(args);
                    for(int i = 0; i < results.length-1; i++) {
                        if(results[i]) {
                            player.sendMessage(prefix + args[i+1] + "wurde entfernt");
                        } else {
                            player.sendMessage(prefix + args[i+1] + "ist nicht vorhanden");
                        }
                    }
                }
            }
        }
        /**
         * Spiel beitreten
         */
        else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                if(getGameOfPlayer(player) == null) {
                    if (tabuGames.containsKey(args[1])) {
                        tabuGames.get(args[1]).joinGame(player.getName());
                    }
                } else {
                    player.sendMessage(prefix + "Du befindest dich bereits in einem Spiel. Nutze /tabu leave");
                }

            } else {
                sender.sendMessage(prefix+"Du kannst diesen Befehl hier nicht ausführen");
            }
        }
        /**
         * Spiel verlassen
         */
        else if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                if(getGameOfPlayer(player) != null) {
                    getGameOfPlayer(player).leaveGame(player.getName());
                } else {
                    player.sendMessage(prefix + "Du befindest dich in keinem Spiel.");
                }
            } else {
                sender.sendMessage(prefix+"Du kannst diesen Befehl hier nicht ausführen.");
            }
        }
        /**
         * Spiel starten
         */
        else if(args.length == 1 && args[0].equalsIgnoreCase("start")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                TabuGame game = getGameOfPlayer(player);
                if(game != null && game.getCreator().getPlayer().equals(player)) {
                    game.start();
                } else if (game == null){
                    player.sendMessage(prefix + "Du befindest dich in keinem Spiel. Nutze /tabu create");
                } else {
                    player.sendMessage(prefix + "Du bist nicht Leiter dieses Spiels.");
                }
            } else {
                sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
            }
        }

        /**
         * Spieler aus laufender Runde kicken
         */
        else if (args.length == 2 && args[0].equalsIgnoreCase("kick")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                TabuGame game = getGameOfPlayer(player);
                if(game == null) {
                    player.sendMessage(prefix + "Du befindet dich in keinem Spiel!");
                } else {
                    if(!game.getCreator().getPlayer().equals(player)) {
                        player.sendMessage(prefix + "Du bist nicht der Leiter dieses Spiels!");
                    } else {
                        if(game.getPlayers().containsKey(args[1])) {
                            game.kickPlayer(game.getPlayers().get(args[1]).getName());
                            player.sendMessage(prefix + args[1] + " wurde aus dem Spiel entfernt.");
                        } else {
                            player.sendMessage(prefix + args[1] + " befindet sich nicht im Spiel.");
                        }
                    }
                }
            } else {
                sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
            }
        }

        /**
         * Spieler aus laufender Runde bannen
         */
        else if(args.length == 2 && args[0].equalsIgnoreCase("ban")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                TabuGame game = getGameOfPlayer(player);
                if(game == null) {
                    player.sendMessage(prefix + "Du befindest dich in keinem Spiel!");
                } else if(!game.getCreator().getPlayer().equals(player)) {
                    player.sendMessage(prefix + "Du bist nicht Leiter dieses Spiels!");
                } else {
                    game.banPlayer(args[1]);
                    player.sendMessage(prefix + args[1] + " wurde aus dem Spiel gebannt!");
                }
            } else {
                sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
            }
        }

        /**
         * Send List of Words to Sender
         */
        else if(args.length == 1 && args[0].equalsIgnoreCase("words")) {
            if(sender instanceof Player) {
                Player player = (Player)sender;
                TabuGame game = getGameOfPlayer(player);
                if(game == null) {
                    player.sendMessage(prefix + "Du befindest dich in keinem Spiel!");
                } else if(!game.getCreator().getPlayer().equals(player)) {
                    player.sendMessage(prefix + "Du bist nicht Leiter dieses Spiels!");
                } else {
                    player.sendMessage(prefix + getListOfWords());
                }
            } else {
                sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
            }
        }
        return true;
    }

    @EventHandler
    public void chatEvent(AsyncPlayerChatEvent e) {
        tabuGames.forEach((keys,value) -> value.chatEvent(e));
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
    public TabuGame getGameOfPlayer(Player player) {
        Set<String> keys = tabuGames.keySet();
        for(String key : keys) {
            if(tabuGames.get(key).getPlayers().containsKey(player.getName())) {
                return tabuGames.get(key);
            }
        }
        return null;
    }
}
