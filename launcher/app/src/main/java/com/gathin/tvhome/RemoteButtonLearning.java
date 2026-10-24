package com.gathin.tvhome;

import android.os.Handler;
import android.os.Looper;
import android.os.SystemClock;
import android.view.KeyEvent;

/** Short-lived in-process key capture; never records text or screen contents. */
final class RemoteButtonLearning {
    interface Listener { void captured(int key, int scan); }
    private static Listener listener;
    private static long until;
    private static int heldKey = -1, heldScan;
    private static long heldUntil;
    private RemoteButtonLearning() { }
    static void begin(Listener next) { listener = next; until = SystemClock.uptimeMillis() + 20_000; }
    static void cancel(Listener owner) { if (listener == owner) listener = null; }
    static boolean consume(KeyEvent event) {
        long now = SystemClock.uptimeMillis();
        if (!RemoteButtonBinding.assignable(event.getKeyCode(), event.getSource())) return false;
        if (event.getKeyCode() == heldKey && event.getScanCode() == heldScan && now < heldUntil) {
            if (event.getAction() == KeyEvent.ACTION_UP) heldKey = -1;
            return true;
        }
        if (listener == null || now >= until || event.getAction() != KeyEvent.ACTION_DOWN
                || event.getRepeatCount() != 0) return false;
        Listener captured = listener;
        listener = null;
        heldKey = event.getKeyCode(); heldScan = event.getScanCode(); heldUntil = now + 2000;
        int key = heldKey, scan = heldScan;
        new Handler(Looper.getMainLooper()).post(() -> captured.captured(key, scan));
        return true;
    }
}
