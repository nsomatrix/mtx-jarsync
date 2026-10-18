package lib;

final class PauseKeyThread extends Thread {
    private final int key;
    private final Canvas canvas;

    PauseKeyThread(Canvas canvas, int key) {
        this.canvas = canvas;
        this.key = key;
    }

    public final void run() {
        try {
            Thread.sleep(Canvas.waittime);
            Display.mc.pause();
        } catch (Exception e) {
            try {
                this.canvas.leyPressed(this.key);
                this.canvas.leyReleased(this.key);
            } catch (Throwable t) {
                Display.mc.showError(t, this);
            }
        }

        Canvas.resetPauseKeyThread(this.canvas);
    }
}
