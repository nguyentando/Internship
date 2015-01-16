package com.example.dont.hometimelinedemo;

import android.os.Handler;
import android.os.Looper;
import android.os.Message;

/**
 * Created by dont on 1/8/2015.
 */
public class Downloader extends Thread {
    public Handler mHandler;

    @Override
    public void run() {
        Looper.prepare();

        mHandler = new Handler() {
            public void handleMessage(Message msg) {

            }
        };

        Looper.loop();
    }

    public void removeMessages(Runnable r) {
        mHandler.removeCallbacks(r);
    }

    public void removeAllMessagesAndCallbacks() {
        mHandler.removeCallbacksAndMessages(null);
    }

    public void post(Runnable r) {
        mHandler.post(r);
    }
}
