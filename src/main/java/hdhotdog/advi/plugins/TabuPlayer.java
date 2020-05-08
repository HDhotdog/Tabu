package hdhotdog.advi.plugins;

import org.bukkit.entity.Player;

public class TabuPlayer{
    private Player player;
    private String name;
    private int points = 0;

    public TabuPlayer(Player player) {
        this.player = player;
        this.name = player.getName();
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return points;
    }
    public void addPoint() {
        this.points++;
    }
}
