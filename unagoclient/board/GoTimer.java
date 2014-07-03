package unagoclient.board;

import unagoclient.StopThread;

/**
 * A timer for the goboard. It will call the alarm method of the board in
 * regular time intervals. This is used to update the timer.
 *
 * @see unagoclient.board.TimedBoard
 */

public class GoTimer extends StopThread {
    public long Interval;
    TimedBoard B;

    public GoTimer(TimedBoard b, long i) {
        this.Interval = i;
        this.B = b;
        this.start();
    }

    @Override
    public void run() {
        try {
            while (!this.stopped()) {
                Thread.sleep(this.Interval);
                this.B.alarm();
            }
        } catch (final Exception e) {
        }
    }
}
