package com.example.user.audio_soundnet.ROOT;

import java.util.Timer;
import java.util.TimerTask;

public class TimerTool {

    Timer timer;
    TimerTask timerTask;
    private int time = 0;

    public TimerTool() {
        timer = new Timer();
    }

    public void StaerTimer(int delay,int period) {
        //if(time != 0) {time = 0;}
        timerTask = new TimerTask() {
            @Override
            public void run() {
                time++;
            }
        };
        timer.schedule(timerTask,delay,period);
    }

    public int StopTimer() {
        if (timerTask != null) {
            timerTask.cancel();
            timerTask = null;
        }
        if (timer != null) {
            timer.cancel();
            timer.purge();
            timer = null;
        }
        return time;
    }
}
