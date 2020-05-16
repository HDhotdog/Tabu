package hdhotdog.adventuria.tabu;

import hdhotdog.adventuria.Main;
import org.bukkit.Bukkit;
import org.bukkit.ChatColor;
import org.bukkit.command.Command;
import org.bukkit.command.CommandExecutor;
import org.bukkit.command.CommandSender;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.EventPriority;
import org.bukkit.event.Listener;
import org.bukkit.event.player.AsyncPlayerChatEvent;
import org.bukkit.event.player.PlayerQuitEvent;
import java.util.*;


public class Tabu implements CommandExecutor, Listener {

    public static Hashtable<String, TabuGame> tabuGames = new Hashtable<>();
    public static String prefix = ChatColor.BLUE + "[TABU] " + ChatColor.GREEN;
    public static Main main;

    public Tabu(Main m) {
        main = m;
    }


    public boolean onCommand(CommandSender sender, Command command, String label, String[] args) {
        /**
         * Spiel erstellen
         */
        if (sender.hasPermission("advi.tabu")) {
            if (args.length == 0) {
                sender.sendMessage(prefix + "Das einzig wahre Tabu Spiel!");
                return true;
            } else if (args[0].equalsIgnoreCase("create") || args[0].equalsIgnoreCase("createevent")) {
                if (!sender.hasPermission("tabu.create.event") && args[0].equalsIgnoreCase("createevent")) {
                    sender.sendMessage(prefix + "Du hast nicht die nötigen Berechtigungen!");
                    return true;
                }
                String gameName = "";
                boolean hasName = false;
                int rounds = 0;
                boolean customRounds = false;
                TabuPlayer creator = null;
                boolean fromPlayer = false;
                if (args.length >= 2) {
                    gameName = args[1];
                    hasName = true;
                }
                if (args.length > 2) {
                    try {
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

                if (sender instanceof Player) {
                    creator = new TabuPlayer((Player) sender);
                    fromPlayer = true;
                }

                TabuGame tabuGame;
                if (hasName && customRounds) {
                    tabuGame = new TabuGame(creator, gameName, rounds);
                } else if (hasName) {
                    tabuGame = new TabuGame(creator, gameName);
                } else {
                    tabuGame = new TabuGame(creator);
                }
                tabuGame.eventGame = args[0].equalsIgnoreCase("createevent");
                tabuGame.setMain(main);
                tabuGame.setTabuInstance(this);
                if (tabuGames.containsKey(tabuGame.getName())) {
                    sender.sendMessage(prefix + "Es existiert bereit ein Spiel mit diesen Namen.");
                    return true;
                }
                tabuGames.put(tabuGame.getName(), tabuGame);

                String creatorName = "Console";
                if (fromPlayer) {
                    creatorName = creator.getName();
                }
                if (rounds == 0) {
                    rounds = 3;
                }
                if (args[0].equalsIgnoreCase("createevent")) {
                    sentToAllOnlinePlayer(ChatColor.GREEN + creatorName + " hat das Tabuevent " + gameName + " mit " + rounds + " Runden gestartet!");
                } else {
                    sentToAllOnlinePlayer(ChatColor.GREEN + creatorName + " hat " + gameName + " mit " + rounds + " Runden gestartet!");
                }


                return true;
            }
            /**
             * Spiele anzeigen
             */
            else if (args.length == 2 && args[0].equalsIgnoreCase("setwarp")) {
                if (sender instanceof Player) {
                    if (!sender.hasPermission("advi.tabu.createevent")) {
                        sender.sendMessage(prefix + "");
                    }
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game == null) {
                        player.sendMessage(prefix + "Du befindest dich in keinem Spiel");
                    } else if (!game.eventGame) {
                        player.sendMessage(prefix + "Dieses Spiel ist kein Event");
                    } else {
                        game.setWarp(args[1]);
                        player.sendMessage(prefix + "Event Warp wurde auf " + args[1] + " gestellt.");
                    }
                }
            } else if (args.length == 1 && args[0].equalsIgnoreCase("list")) {
                sender.sendMessage(prefix + "Aktuelle Spiele:");
                tabuGames.forEach((keys, values) -> sender.sendMessage(ChatColor.GREEN + "* " + values.getName()));
            }
            /**
             * Spiel beenden
             */
            else if (args.length == 1 && args[0].equalsIgnoreCase("quit")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    Set<String> keys = tabuGames.keySet();
                    for (String key : keys) {
                        if (tabuGames.get(key).getCreator().getPlayer().equals(player)) {
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
            else if (args.length >= 2 && args[0].equalsIgnoreCase("add")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game != null) {
                        args[0] = null;
                        boolean[] results = game.addWords(args);
                        for (int i = 0; i < results.length; i++) {
                            if (results[i]) {
                                player.sendMessage(prefix + args[i + 1] + " wurde hinzugefügt");
                            } else {
                                player.sendMessage(prefix + args[i + 1] + " existiert bereits");
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
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game != null) {
                        args[0] = null;
                        boolean[] results = game.removeWords(args);
                        for (int i = 0; i < results.length; i++) {
                            if (results[i]) {
                                player.sendMessage(prefix + args[i + 1] + " wurde entfernt");
                            } else {
                                player.sendMessage(prefix + args[i + 1] + "ist nicht vorhanden");
                            }
                        }
                    }
                }
            }
            /**
             * Spiel beitreten
             */
            else if (args.length == 2 && args[0].equalsIgnoreCase("join")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (getGameOfPlayer(player) == null) {
                        if (tabuGames.containsKey(args[1])) {
                            tabuGames.get(args[1]).joinGame(player.getName());
                        }
                    } else {
                        player.sendMessage(prefix + "Du befindest dich bereits in einem Spiel. Nutze /tabu leave");
                    }

                } else {
                    sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen");
                }
            }
            /**
             * Spiel verlassen
             */
            else if (args.length == 1 && args[0].equalsIgnoreCase("leave")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    if (getGameOfPlayer(player) != null) {
                        getGameOfPlayer(player).leaveGame(player.getName());
                    } else {
                        player.sendMessage(prefix + "Du befindest dich in keinem Spiel.");
                    }
                } else {
                    sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
                }
            }
            /**
             * Spiel starten
             */
            else if (args.length == 1 && args[0].equalsIgnoreCase("start")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game != null && game.getCreator().getPlayer().equals(player)) {
                        if (game.words.size() == 0) {
                            player.sendMessage(prefix + "Keine Wörter vorhanden");
                        } else {
                            if (game.eventGame && game.eventWarp == null) {
                                player.sendMessage(prefix + "Es ist noch kein Warp für dieses Spiel gesetzt! Nutze /tabu setwarp <warp>");
                            } else {
                                game.start();
                            }

                        }
                    } else if (game == null) {
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
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game == null) {
                        player.sendMessage(prefix + "Du befindet dich in keinem Spiel!");
                    } else {
                        if (!game.getCreator().getPlayer().equals(player)) {
                            player.sendMessage(prefix + "Du bist nicht der Leiter dieses Spiels!");
                        } else {
                            if (game.getPlayers().containsKey(args[1])) {
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
            else if (args.length == 2 && args[0].equalsIgnoreCase("ban")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game == null) {
                        player.sendMessage(prefix + "Du befindest dich in keinem Spiel!");
                    } else if (!game.getCreator().getPlayer().equals(player)) {
                        player.sendMessage(prefix + "Du bist nicht Leiter dieses Spiels!");
                    } else {
                        if (game.banPlayer(args[1])) {
                            player.sendMessage(prefix + args[1] + " wurde aus dem Spiel gebannt!");
                        }
                    }
                } else {
                    sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
                }
            } else if (args.length == 2 && args[0].equalsIgnoreCase("unban")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game == null) {
                        player.sendMessage(prefix + "Du befindest dich in keinem Spiel!");
                    } else if (!game.getCreator().getPlayer().equals(player)) {
                        player.sendMessage(prefix + "Du bist nicht Leitet dieses Spiels!");
                    } else {
                        if (game.unbanPlayer(args[1])) {
                            game.sendMessageToAllPlayers(args[1] + " wurde entbannt.");
                        }
                    }
                }
            }
            /**
             * Send List of Words to Sender
             */
            else if (args.length == 1 && args[0].equalsIgnoreCase("words")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game == null) {
                        player.sendMessage(prefix + "Du befindest dich in keinem Spiel!");
                    } else if (!game.getCreator().getPlayer().equals(player)) {
                        player.sendMessage(prefix + "Du bist nicht Leiter dieses Spiels!");
                    } else {
                        player.sendMessage(prefix + game.getWords());
                    }
                } else {
                    sender.sendMessage(prefix + "Du kannst diesen Befehl hier nicht ausführen.");
                }
            }
            /**
             * Spielinfo anzeigen
             */
            else if (args.length == 1 && args[0].equalsIgnoreCase("info")) {
                if (sender instanceof Player) {
                    Player player = (Player) sender;
                    TabuGame game = getGameOfPlayer(player);
                    if (game == null) {
                        player.sendMessage(prefix + "Du befindest dich in keinem Spiel.");
                    } else {
                        player.sendMessage(game.toString());
                    }
                }
            } else {
                sender.sendMessage(prefix + "Unbekannter Befehl");
            }
            return true;
        }
        sender.sendMessage(prefix + "Du hast keine Rechte auf diesen Befehl");
        return true;
        }

        @EventHandler(priority = EventPriority.NORMAL)
        public void chatEvent (AsyncPlayerChatEvent e){
            ArrayList<TabuGame> list = new ArrayList<>(tabuGames.values());
            for (TabuGame game : list) {
                if (game != null) {
                    game.chatEvent(e);
                }
            }
        }
        @EventHandler
        public void quitEvent (PlayerQuitEvent e){
            if(getGameOfPlayer(e.getPlayer()) == null) {
              return;
            }
            ArrayList<TabuGame> list = new ArrayList<>(tabuGames.values());
            for (TabuGame game : list) {
                if (game != null) {
                    game.quitEvent(e);
                }
            }
        }

        public void sentToAllOnlinePlayer (String message){
            for (Player p : Bukkit.getOnlinePlayers()) {
                p.sendMessage(prefix + message);
            }
        }
        public TabuGame getGameOfPlayer (Player player){
            Set<String> keys = tabuGames.keySet();
            for (String key : keys) {
                if (tabuGames.get(key).getPlayers().containsKey(player.getName())) {
                    return tabuGames.get(key);
                }
            }
            return null;
        }
    }


