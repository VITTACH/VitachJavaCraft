package com.vittach.jumpjack.framework;

import java.io.Serializable;

public class MyTimer implements Serializable {
    private long endTime;
    private long startTime;
    private long currentTime;

    public void start(long timeOffset) {
        this.endTime = timeOffset;
        startTime = System.currentTimeMillis();
    }

    public boolean isActive(TimerListener timerListener) {
        if (endTime > 0) {
            if ((currentTime = System.currentTimeMillis() - startTime) < endTime) {
                return true;
            } else {
                timerListener.onTimerStopped();
                reset();
            }
        }
        return false;
    }

    public float getCurTime() {
        return (endTime == 0) ? 0 : currentTime / 1000f;
    }

    public void reset() {
        endTime = 0;
    }
}
