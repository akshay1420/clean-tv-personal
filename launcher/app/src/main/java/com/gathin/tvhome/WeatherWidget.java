package com.gathin.tvhome;

import android.content.Context;
import android.content.Intent;
import android.database.ContentObserver;
import android.database.Cursor;
import android.net.Uri;
import android.os.Handler;
import android.os.Looper;
import android.view.View;
import android.widget.TextView;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

/** Reads a small signed companion's cached weather; the launcher has no Internet permission. */
final class WeatherWidget {
    static final String SHOW = "widget_weather", CLOCK = "widget_clock";
    private static final Uri URI = Uri.parse("content://com.gathin.tvweather/current");
    private final Context context;
    private final TextView view;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final ExecutorService worker = Executors.newSingleThreadExecutor();
    private boolean active, registered, loading, closed, pendingRefresh;
    private final ContentObserver observer = new ContentObserver(handler) {
        @Override public void onChange(boolean selfChange) {
            if (loading) pendingRefresh = true;
            else refresh();
        }
    };
    private final Runnable tick = new Runnable() {
        @Override public void run() { refresh(); if (active) handler.postDelayed(this, 30 * 60_000L); }
    };

    WeatherWidget(Context context, TextView view) {
        this.context = context; this.view = view;
        view.setOnClickListener(v -> openDetails(context));
    }
    static void openDetails(Context context) {
        Intent intent = context.getPackageManager().getLeanbackLaunchIntentForPackage("com.gathin.tvweather");
        if (intent != null) try { context.startActivity(intent); }
        catch (android.content.ActivityNotFoundException | SecurityException ignored) { }
    }
    void start() {
        active = PersonalAppearance.prefs(context).getBoolean(SHOW, true);
        view.setVisibility(active ? View.VISIBLE : View.GONE);
        if (!active || closed) return;
        if (!registered) try {
            context.getContentResolver().registerContentObserver(URI, false, observer);
            registered = true;
        } catch (RuntimeException ignored) { }
        handler.removeCallbacks(tick); handler.post(tick);
    }
    private void refresh() {
        if (!active || closed || loading) return;
        loading = true;
        worker.execute(() -> {
            String text = context.getString(R.string.widget_weather_unavailable);
            try (Cursor cursor = context.getContentResolver().query(URI, null, null, null, null)) {
                if (cursor != null && cursor.moveToFirst()) {
                    String summary = cursor.getString(cursor.getColumnIndexOrThrow("summary"));
                    String city = cursor.getString(cursor.getColumnIndexOrThrow("city"));
                    long observed = cursor.getLong(cursor.getColumnIndexOrThrow("observed"));
                    int fetching = cursor.getInt(cursor.getColumnIndexOrThrow("loading"));
                    long now = System.currentTimeMillis();
                    boolean stale = observed > 0 && (observed > now + 30 * 60_000L || now - observed > 2 * 60 * 60_000L);
                    String air = "US AQI unavailable";
                    int airColumn = cursor.getColumnIndex("aqi_summary");
                    int airTimeColumn = cursor.getColumnIndex("aqi_observed");
                    if (airColumn >= 0 && airTimeColumn >= 0) {
                        String airSummary = cursor.getString(airColumn);
                        long airTime = cursor.getLong(airTimeColumn);
                        if (airTime > 0 && airSummary != null && !airSummary.isEmpty()) {
                            int separator = airSummary.indexOf(" · ");
                            air = (separator < 0 ? airSummary : airSummary.substring(0, separator)) + " (model)";
                            if (airTime > now + 60 * 60_000L || now - airTime > 3 * 60 * 60_000L) air += " · " + context.getString(R.string.widget_cached);
                        }
                    }
                    text = observed > 0 ? summary + (stale ? " · " + context.getString(R.string.widget_cached) : "")
                            + " · " + air + "\n" + city + " · Open-Meteo"
                            : context.getString(fetching != 0 ? R.string.widget_weather_loading : R.string.widget_weather_unavailable);
                }
            } catch (RuntimeException ignored) { }
            String display = text;
            handler.post(() -> {
                loading = false;
                if (active && !closed) view.setText(display);
                if (pendingRefresh) { pendingRefresh = false; refresh(); }
            });
        });
    }
    void stop() {
        active = false; handler.removeCallbacks(tick);
        if (registered) { context.getContentResolver().unregisterContentObserver(observer); registered = false; }
    }
    void close() { stop(); closed = true; worker.shutdownNow(); }
}
