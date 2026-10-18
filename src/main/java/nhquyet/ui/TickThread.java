package nhquyet.ui;

/**
 * Single-shot timer thread for clearing index count displays.
 */
public final class TickThread extends Thread {
    private final CustomChoiceCanvas canvas;

    public TickThread(CustomChoiceCanvas canvas) {
        this.canvas = canvas;
    }

    public void run() {
        try {
            Thread.sleep(1000L);
        } catch (Exception e) {
        }
        this.canvas.showIndexCount = false;
        this.canvas.repaint();
    }
}
