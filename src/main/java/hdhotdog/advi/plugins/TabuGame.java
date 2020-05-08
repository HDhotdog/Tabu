package hdhotdog.advi.plugins;

import org.bukkit.entity.Player;
import sun.tools.jconsole.Tab;

import java.util.ArrayList;
import java.util.HashSet;

public class TabuGame {
    public HashSet<TabuPlayer> players = new HashSet<TabuPlayer>();
    private String name;
    private int rounds;
    private int gameID;
    private static int games = 0;

    public TabuGame(String name, int rounds) {
        this.name = name;
        this.rounds = rounds;
        this.gameID = games;
        games++;

    }

    public TabuGame(String name) {
        this(name, 3);
    }

    public TabuGame(){
        this("Tabu-Game" + games, 3);
    }




}
