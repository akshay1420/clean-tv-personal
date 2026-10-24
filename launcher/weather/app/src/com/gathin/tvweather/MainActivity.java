package com.gathin.tvweather;

import android.app.Activity;
import android.database.ContentObserver;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.text.method.LinkMovementMethod;
import android.view.Gravity;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.text.DateFormat;
import java.util.Date;

public final class MainActivity extends Activity {
    private TextView weather, status, aqi, aqiStatus;
    private final ContentObserver observer = new ContentObserver(new Handler()) {
        @Override public void onChange(boolean selfChange) { render(); }
    };
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        LinearLayout layout = new LinearLayout(this);
        layout.setOrientation(LinearLayout.VERTICAL);
        layout.setGravity(Gravity.CENTER_VERTICAL);
        int pad = (int) (48 * getResources().getDisplayMetrics().density);
        layout.setPadding(pad, pad / 2, pad, pad / 2);
        layout.setBackgroundColor(Color.rgb(16, 24, 36));
        label(layout, WeatherStore.CITY, 32);
        label(layout, BuildConfig.WEATHER_LATITUDE.isEmpty() ? "Configure weather.properties locally and rebuild the companion." : "Approximate area: " + BuildConfig.WEATHER_LATITUDE + ", " + BuildConfig.WEATHER_LONGITUDE, 18);
        LinearLayout readings = new LinearLayout(this);
        layout.addView(readings);
        LinearLayout weatherColumn = new LinearLayout(this);
        weatherColumn.setOrientation(LinearLayout.VERTICAL);
        readings.addView(weatherColumn, new LinearLayout.LayoutParams(0, -2, 1));
        LinearLayout aqiColumn = new LinearLayout(this);
        aqiColumn.setOrientation(LinearLayout.VERTICAL);
        readings.addView(aqiColumn, new LinearLayout.LayoutParams(0, -2, 1));
        weather = label(weatherColumn, "", 32);
        status = label(weatherColumn, "", 16);
        aqi = label(aqiColumn, "", 26);
        aqiStatus = label(aqiColumn, "", 16);
        label(aqiColumn, "Model estimate · US AQI scale, not a local station reading\nCAMS global model (~45 km); approximate area", 14);
        Button refresh = new Button(this);
        refresh.setText("Refresh weather & AQI");
        refresh.setOnClickListener(view -> { WeatherStore.get(this).refresh(true); render(); });
        layout.addView(refresh, new LinearLayout.LayoutParams((int) (320 * getResources().getDisplayMetrics().density), (int) (56 * getResources().getDisplayMetrics().density)));
        TextView attribution = label(layout, "Weather: Open-Meteo · Air quality: Open-Meteo and CAMS ENSEMBLE · CC BY 4.0\nhttps://open-meteo.com/en/docs/air-quality-api\nhttps://creativecommons.org/licenses/by/4.0/", 14);
        attribution.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        attribution.setMovementMethod(LinkMovementMethod.getInstance());
        label(layout, "Fetched when opened or requested by Home; refreshes after 30 minutes.\nNo GPS, account or API key. Refresh retries pause for 5 minutes after an error.", 16);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(layout);
        setContentView(scroll);
        refresh.requestFocus();
    }
    private TextView label(LinearLayout layout, String text, int size) {
        TextView label = new TextView(this);
        label.setText(text);
        label.setTextSize(size);
        label.setTextColor(Color.WHITE);
        label.setPadding(0, 5, 0, 5);
        layout.addView(label);
        return label;
    }
    @Override public void onStart() {
        super.onStart();
        getContentResolver().registerContentObserver(WeatherStore.URI, false, observer);
        WeatherStore.get(this).refresh(false);
        render();
    }
    @Override public void onStop() { getContentResolver().unregisterContentObserver(observer); super.onStop(); }
    private String date(long millis) { return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(millis)); }
    private void render() {
        Object[] row = WeatherStore.get(this).row();
        long observed = (Long) row[2], updated = (Long) row[3];
        boolean loading = (Integer) row[4] == 1, error = (Integer) row[5] == 1;
        weather.setText(updated > 0 ? (String) row[1] : "Weather unavailable");
        String text = updated > 0 ? "Observed: " + date(observed) + "\nFetched: " + date(updated) : "No saved observation yet.";
        if (loading) text += "\nRefreshing…";
        else if (error) text += updated > 0 ? "\nRefresh failed · Showing saved weather" : "\nUnable to fetch weather. Try again later.";
        else if (updated > 0 && !WeatherPolicy.fresh(updated, System.currentTimeMillis(), WeatherStore.MAX_AGE)) text += "\nSaved weather is over 30 minutes old.";
        status.setText(text);
        long aqiObserved = (Long) row[7], aqiUpdated = (Long) row[8];
        boolean aqiError = (Integer) row[9] == 1;
        aqi.setText(aqiUpdated > 0 ? (String) row[6] : "US AQI unavailable");
        String aqiText = aqiUpdated > 0 ? "Modeled for: " + date(aqiObserved) + "\nFetched: " + date(aqiUpdated) : "No saved AQI estimate yet.";
        if (loading) aqiText += "\nRefreshing…";
        else if (aqiError) aqiText += aqiUpdated > 0 ? "\nRefresh failed · Showing saved AQI" : "\nUnable to fetch AQI. Try again later.";
        else if (aqiUpdated > 0 && !WeatherPolicy.fresh(aqiUpdated, System.currentTimeMillis(), WeatherStore.MAX_AGE)) aqiText += "\nSaved AQI is over 30 minutes old.";
        aqiStatus.setText(aqiText);
    }
}
