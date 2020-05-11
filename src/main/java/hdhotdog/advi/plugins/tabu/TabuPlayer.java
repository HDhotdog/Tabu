package hdhotdog.advi.plugins.tabu;

import org.bukkit.entity.Player;

public class TabuPlayer{
    private Player player;
    private String name;
    private int points;
    private boolean joinedGame;

    public TabuPlayer(Player player) {
        this.player = player;
        this.name = player.getName();
        this.points = 0;
    }

    public Player getPlayer() {
        return player;
    }

    public String getName() {
        return name;
    }

    public int getPoints() {
        return this.points;
    }
    public void addPoint() {
        this.points++;
    }
    public boolean hasJoined() {
        return this.joinedGame;
    }
    public void joinedGame(boolean joined) {
        this.joinedGame = joined;
    }

    public void sendMessage(String s) {
        this.player.sendMessage(s);
    }
}
