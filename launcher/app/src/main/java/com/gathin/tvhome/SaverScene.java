package com.gathin.tvhome;

import android.content.Context;
import android.os.Handler;
import android.os.Looper;
import android.view.LayoutInflater;
import android.widget.FrameLayout;
import java.io.File;
import java.util.List;

/** The same selected screensaver for Home idle and Android's system dream. */
final class SaverScene {
    private final ClockScene clock;
    private final WallpaperPlayer photos;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable tick = new Runnable() {
        @Override public void run() {
            if (photos == null) return;
            photos.update();
            handler.postDelayed(this, 30_000);
        }
    };

    SaverScene(Context context, FrameLayout root) {
        root.setBackgroundColor(0xFF000000);
        String style = PersonalAppearance.saver(context);
        List<File> files = PersonalAppearance.photos(context, style);
        if (("nature".equals(style) || "world".equals(style) || "nightlife".equals(style)) && !files.isEmpty()) {
            photos = new WallpaperPlayer(context, root, Config.load(context), files);
            clock = null;
        } else {
            photos = null;
            LayoutInflater.from(context).inflate(R.layout.dream_clock, root, true);
            clock = new ClockScene(root);
            clock.useHomeWallpaper(context);
        }
    }
    void start() {
        if (clock != null) clock.start();
        if (photos != null) {
            photos.start();
            handler.removeCallbacks(tick);
            handler.postDelayed(tick, 30_000);
        }
    }
    void stop() {
        handler.removeCallbacks(tick);
        if (clock != null) clock.stop();
        if (photos != null) photos.stop();
    }
    void close() { stop(); if (photos != null) photos.close(); }
}
