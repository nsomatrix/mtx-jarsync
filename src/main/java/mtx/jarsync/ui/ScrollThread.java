package mtx.jarsync.ui;

/**
 * Background worker thread for smooth marquee text scrolling on CustomChoiceCanvas.
 */
public final class ScrollThread extends Thread {
    private final CustomChoiceCanvas canvas;

    public ScrollThread(CustomChoiceCanvas canvas) {
        this.canvas = canvas;
    }

    public void run() {
        while (this.canvas.isScrolling) {
            try {
                Thread.sleep(500L);
                this.canvas.repaint();
            } catch (Exception e) {
            }
        }
    }
}
