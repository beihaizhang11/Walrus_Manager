package com.example.walrus_manager.util;

import android.os.Handler;
import android.os.Looper;

import java.util.ArrayList;
import java.util.List;

public class CountdownTimer {
    private static CountdownTimer instance;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final List<CountdownListener> listeners = new ArrayList<>();
    private final Runnable updateRunnable = new Runnable() {
        @Override
        public void run() {
            notifyListeners();
            handler.postDelayed(this, 1000); // 每秒更新一次
        }
    };

    public interface CountdownListener {
        void onCountdownTick();
    }

    private CountdownTimer() {
        // 私有构造函数
    }

    public static synchronized CountdownTimer getInstance() {
        if (instance == null) {
            instance = new CountdownTimer();
        }
        return instance;
    }

    public void addListener(CountdownListener listener) {
        if (listeners.isEmpty()) {
            // 第一个监听器被添加时开始更新
            handler.post(updateRunnable);
        }
        if (!listeners.contains(listener)) {
            listeners.add(listener);
        }
    }

    public void removeListener(CountdownListener listener) {
        listeners.remove(listener);
        if (listeners.isEmpty()) {
            // 没有监听器时停止更新
            handler.removeCallbacks(updateRunnable);
        }
    }

    private void notifyListeners() {
        for (CountdownListener listener : listeners) {
            listener.onCountdownTick();
        }
    }
} 