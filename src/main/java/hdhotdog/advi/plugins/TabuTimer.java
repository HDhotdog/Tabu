package hdhotdog.advi.plugins;

public class TabuTimer extends Thread {
    private TabuGame game;
    public TabuTimer(TabuGame game) {
        this.game = game;
    }
    @Override
    public void start() {
        try {
            this.wait(30000);
            game.sendMessageToAllPlayers(game.prefix() + "Noch 90 Sekunden");
            this.wait(30000);
            game.sendMessageToAllPlayers(game.prefix() +"Noch 60 Sekunden");
            this.wait(30000);
            game.sendMessageToAllPlayers(game.prefix() +"Noch 30 Sekunden");
            this.wait(20000);
            game.sendMessageToAllPlayers(game.prefix() +"Noch 10 Sekunden");
            this.wait(7000);
            game.sendMessageToAllPlayers(game.prefix() +"3");
            this.wait(1000);
            game.sendMessageToAllPlayers(game.prefix() +"2");
            this.wait(1000);
            game.sendMessageToAllPlayers(game.prefix() +"1");
            this.wait(1000);
            game.sendMessageToAllPlayers(game.prefix() +"Ende!");
            this.end();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }

    public void end() {
        game.endRound();
        this.stop();
    }
}
