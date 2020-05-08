package hdhotdog.advi.plugins;

import org.bukkit.entity.Player;

public class TabuPlayer{
    private Player player;
    private String name;
    private int points;

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

    public void addPoints(int points){
        this.points += points;
    }

    public void subtractPoint(){
        this.points--;
    }

    public void subtractPoints(int points){
        this.points -= points;
    }
}
