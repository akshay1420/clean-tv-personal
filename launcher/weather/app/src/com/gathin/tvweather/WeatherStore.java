package com.gathin.tvweather;

import android.content.Context;
import android.content.SharedPreferences;
import android.net.Uri;
import org.json.JSONObject;
import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.net.URL;
import java.util.Locale;
import java.util.List;
import java.util.ArrayList;
import java.net.URLEncoder;
import org.json.JSONArray;
import java.util.concurrent.atomic.AtomicBoolean;
import javax.net.ssl.HttpsURLConnection;

/** A demand-driven cache: no scheduled tasks or background location service. */
final class WeatherStore {
    static final Uri URI = Uri.parse("content://com.gathin.tvweather/current");
    static final long MAX_AGE = 30 * 60 * 1000L;
    private static final long RETRY_DELAY = 5 * 60 * 1000L;
    private static WeatherStore instance;
    private final Context context;
    private final SharedPreferences prefs;
    private final AtomicBoolean loading = new AtomicBoolean();
    private WeatherLocation location;
    private String summary;
    private long observed, updated, attempted;
    private boolean error;
    private String aqiSummary;
    private long aqiObserved, aqiUpdated, aqiAttempted;
    private boolean aqiError;

    static synchronized WeatherStore get(Context context) {
        if (instance == null) instance = new WeatherStore(context.getApplicationContext());
        return instance;
    }

    private WeatherStore(Context context) {
        this.context = context;
        prefs = context.getSharedPreferences("weather", Context.MODE_PRIVATE);
        try {
            location=new WeatherLocation(prefs.getString("location_city",BuildConfig.WEATHER_CITY),
                Double.parseDouble(prefs.getString("location_lat",BuildConfig.WEATHER_LATITUDE)),
                Double.parseDouble(prefs.getString("location_lon",BuildConfig.WEATHER_LONGITUDE)));
        } catch(IllegalArgumentException missing) { location=null; }
        summary = prefs.getString("summary", "");
        observed = prefs.getLong("observed", 0);
        updated = prefs.getLong("updated", 0);
        attempted = prefs.getLong("attempted", 0);
        error = prefs.getBoolean("error", false);
        aqiSummary = prefs.getString("aqi_summary", "");
        aqiObserved = prefs.getLong("aqi_observed", 0);
        aqiUpdated = prefs.getLong("aqi_updated", 0);
        aqiAttempted = prefs.getLong("aqi_attempted", 0);
        aqiError = prefs.getBoolean("aqi_error", false);
    }

    synchronized Object[] row() {
        return new Object[] { location==null ? "Choose a weather area" : location.city, summary, observed, updated, loading.get() ? 1 : 0, error ? 1 : 0,
            aqiSummary, aqiObserved, aqiUpdated, aqiError ? 1 : 0 };
    }

    synchronized void refresh(boolean force) {
        final WeatherLocation chosen=location;
        if (chosen==null) return;
        long now = System.currentTimeMillis();
        boolean weatherDue = (force || error || !WeatherPolicy.fresh(updated, now, MAX_AGE))
            && !WeatherPolicy.fresh(attempted, now, error ? RETRY_DELAY : 60 * 1000L);
        boolean aqiDue = (force || aqiError || !WeatherPolicy.fresh(aqiUpdated, now, MAX_AGE))
            && !WeatherPolicy.fresh(aqiAttempted, now, aqiError ? RETRY_DELAY : 60 * 1000L);
        if ((!weatherDue && !aqiDue) || !loading.compareAndSet(false, true)) return;
        if (weatherDue) attempted = now;
        if (aqiDue) aqiAttempted = now;
        new Thread(() -> {
            try {
                if (weatherDue) fetchWeather(chosen);
                if (aqiDue) fetchAqi(chosen);
            } finally {
                loading.set(false);
                context.getContentResolver().notifyChange(URI, null);
            }
        }, "weather-refresh").start();
    }

    private static JSONObject request(String endpoint) throws Exception {
        HttpsURLConnection connection = null;
        try {
            connection = (HttpsURLConnection) new URL(endpoint).openConnection();
            connection.setConnectTimeout(6000);
            connection.setReadTimeout(6000);
            connection.setInstanceFollowRedirects(false);
            connection.setRequestProperty("Accept", "application/json");
            if (connection.getResponseCode() != 200) throw new Exception("HTTP failure");
            if (connection.getContentLengthLong() > 65536) throw new Exception("Response too large");
            ByteArrayOutputStream bytes = new ByteArrayOutputStream();
            try (InputStream input = connection.getInputStream()) {
                byte[] buffer = new byte[4096];
                int count;
                while ((count = input.read(buffer)) != -1) {
                    if (bytes.size() + count > 65536) throw new Exception("Response too large");
                    bytes.write(buffer, 0, count);
                }
            }
            return new JSONObject(bytes.toString("UTF-8"));
        } finally {
            if (connection != null) connection.disconnect();
        }
    }

    private void fetchWeather(WeatherLocation chosen) {
        try {
            JSONObject root = request("https://api.open-meteo.com/v1/forecast?latitude="+chosen.latitude+"&longitude="+chosen.longitude+"&current=temperature_2m,weather_code,is_day&timeformat=unixtime&timezone=UTC");
            JSONObject current = root.getJSONObject("current");
            JSONObject units = root.getJSONObject("current_units");
            double temperature = current.getDouble("temperature_2m");
            double rawCode = current.getDouble("weather_code");
            int code = (int) rawCode;
            long seconds = current.getLong("time");
            long now = System.currentTimeMillis();
            if (!WeatherPolicy.validTemperature(temperature) || rawCode != code
                    || WeatherPolicy.condition(code).equals("Conditions unavailable")
                    || !"°C".equals(units.getString("temperature_2m"))
                    || !"unixtime".equals(units.getString("time"))
                    || seconds < (now - 24 * 60 * 60 * 1000L) / 1000
                    || seconds > (now + 60 * 60 * 1000L) / 1000) throw new Exception("Invalid observation");
            synchronized (this) {
                summary = String.format(Locale.US, "%.1f°C · %s", temperature, WeatherPolicy.condition(code));
                observed = seconds * 1000L;
                updated = now;
                error = false;
                save();
            }
        } catch (Exception failure) {
            synchronized (this) {
                error = true;
                save(); // Retain the last good observation and its original fetch time.
            }
        } finally {
            context.getContentResolver().notifyChange(URI, null);
        }
    }

    private void fetchAqi(WeatherLocation chosen) {
        try {
            JSONObject root = request("https://air-quality-api.open-meteo.com/v1/air-quality?latitude="+chosen.latitude+"&longitude="+chosen.longitude+"&current=us_aqi,pm2_5,pm10&timeformat=unixtime&timezone=UTC");
            JSONObject current = root.getJSONObject("current");
            JSONObject units = root.getJSONObject("current_units");
            double value = current.getDouble("us_aqi");
            long seconds = current.getLong("time");
            long now = System.currentTimeMillis();
            if (!WeatherPolicy.validAqi(value) || !"USAQI".equals(units.getString("us_aqi"))
                    || !"unixtime".equals(units.getString("time"))
                    || seconds < (now - 24 * 60 * 60 * 1000L) / 1000
                    || seconds > (now + 60 * 60 * 1000L) / 1000) throw new Exception("Invalid AQI observation");
            int rounded = (int) Math.round(value);
            synchronized (this) {
                aqiSummary = "US AQI " + rounded + " · " + WeatherPolicy.aqiCategory(rounded);
                aqiObserved = seconds * 1000L;
                aqiUpdated = now;
                aqiError = false;
                save();
            }
        } catch (Exception failure) {
            synchronized (this) {
                aqiError = true;
                save();
            }
        } finally {
            context.getContentResolver().notifyChange(URI, null);
        }
    }

    synchronized WeatherLocation location() { return location; }

    synchronized boolean setLocation(WeatherLocation selected) {
        if(loading.get()) return false; // Never mix observations from two locations.
        location=selected; summary=""; aqiSummary="";
        observed=updated=attempted=aqiObserved=aqiUpdated=aqiAttempted=0;
        error=aqiError=false;
        prefs.edit().clear().putString("location_city",selected.city)
            .putString("location_lat",Double.toString(selected.latitude))
            .putString("location_lon",Double.toString(selected.longitude)).apply();
        context.getContentResolver().notifyChange(URI,null);
        refresh(true);
        return true;
    }

    static List<WeatherLocation> search(String query) throws Exception {
        query=query.trim();
        if(query.length()<2 || query.length()>100) throw new IllegalArgumentException("Enter a city name");
        JSONObject root=request("https://geocoding-api.open-meteo.com/v1/search?name="+URLEncoder.encode(query,"UTF-8")+"&count=8&language=en&format=json");
        JSONArray results=root.optJSONArray("results");
        List<WeatherLocation> areas=new ArrayList<>();
        if(results!=null) for(int i=0;i<Math.min(8,results.length());i++) {
            JSONObject item=results.getJSONObject(i);
            String name=item.getString("name");
            String region=item.optString("admin1","");
            String country=item.optString("country","");
            String label=name+(region.isEmpty() || region.equals(name) ? "" : ", "+region)+(country.isEmpty() ? "" : ", "+country);
            try { areas.add(new WeatherLocation(label,item.getDouble("latitude"),item.getDouble("longitude"))); }
            catch(IllegalArgumentException invalid) { /* skip malformed provider result */ }
        }
        return areas;
    }

    private void save() {
        prefs.edit().putString("summary", summary).putLong("observed", observed)
                .putLong("updated", updated).putLong("attempted", attempted).putBoolean("error", error)
                .putString("aqi_summary", aqiSummary).putLong("aqi_observed", aqiObserved)
                .putLong("aqi_updated", aqiUpdated).putLong("aqi_attempted", aqiAttempted)
                .putBoolean("aqi_error", aqiError).apply();
    }
}
