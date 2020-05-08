package hdhotdog.advi.plugins;

public class TabuTimer implements Runnable {
    public TabuGame tabuGame;
    private boolean running;
    private Thread thread;

    public TabuTimer(TabuGame tabuGame) {
        this.tabuGame = tabuGame;
        this.thread = new Thread(this);
    }

    public void start() {
        this.running = true;
        if(running) {
            this.thread.start();
        }
    }
    public void run() {
        try {
            thread.wait(30000);
            tabuGame.tellRemainingTime("1:30 Minuten");
            thread.wait(30000);
            tabuGame.tellRemainingTime("1 Minute");
            thread.wait(30000);
            tabuGame.tellRemainingTime("30 Sekunden");
            thread.wait(20000);
            tabuGame.tellRemainingTime("10 Sekunden");
            thread.wait(10000);
            tabuGame.endRound();
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        this.running = false;
        this.thread.stop();
    }
}
