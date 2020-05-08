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
            thread.wait(30000);
            thread.wait(30000);
            thread.wait(30000);
        } catch (InterruptedException e) {
            e.printStackTrace();
        }
    }
    public void stop() {
        this.running = false;
        this.thread.stop();
    }
}
