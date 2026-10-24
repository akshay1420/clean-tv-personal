package com.gathin.tvweather;

import android.app.Activity;
import android.app.AlertDialog;
import android.Manifest;
import android.content.pm.PackageManager;
import android.location.Location;
import android.location.LocationListener;
import android.location.LocationManager;
import android.widget.EditText;
import android.widget.Toast;
import java.util.List;
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
    private TextView weather, status, aqi, aqiStatus, areaTitle, areaDetails;
    private final Handler locationHandler=new Handler(android.os.Looper.getMainLooper());
    private LocationManager locationManager;
    private LocationListener locationListener;
    private boolean searching, started;
    private final Runnable locationTimeout=() -> {
        stopLocation(); toast("TV location unavailable. Choose a city instead.");
    };
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
        areaTitle=label(layout, "", 32);
        areaDetails=label(layout,"",18);
        Button current=new Button(this);
        current.setText("Use TV location (optional)");
        current.setOnClickListener(v -> useTvLocation());
        layout.addView(current);
        Button choose=new Button(this);
        choose.setText("Choose a city"); choose.setOnClickListener(v -> chooseCity());
        layout.addView(choose);
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
        TextView attribution = label(layout, "Weather: Open-Meteo · Air quality: Open-Meteo and CAMS ENSEMBLE · CC BY 4.0\nCity search: GeoNames via Open-Meteo\nhttps://open-meteo.com/en/docs/air-quality-api\nhttps://creativecommons.org/licenses/by/4.0/", 14);
        attribution.setAutoLinkMask(android.text.util.Linkify.WEB_URLS);
        attribution.setMovementMethod(LinkMovementMethod.getInstance());
        label(layout, "Fetched when opened or requested by Home; refreshes after 30 minutes.\nTV location is optional and used only when requested; city search needs no location permission.\nChosen area is saved on this TV and sent to Open-Meteo for forecasts. No background location tracking.\nRefresh retries pause for 5 minutes after an error.", 16);
        ScrollView scroll = new ScrollView(this);
        scroll.setFillViewport(true);
        scroll.addView(layout);
        setContentView(scroll);
        refresh.requestFocus();
    }
    private void toast(String message) { Toast.makeText(this,message,Toast.LENGTH_LONG).show(); }
    private void saveArea(WeatherLocation area) {
        if(!WeatherStore.get(this).setLocation(area)) toast("A weather update is running. Try again in a few seconds.");
        render();
    }
    private void chooseCity() {
        if(searching) { toast("City search is already running."); return; }
        EditText entry=new EditText(this);
        entry.setSingleLine(true); entry.setHint("City, country");
        new AlertDialog.Builder(this).setTitle("Choose a weather city")
            .setMessage("Search text is sent to Open-Meteo. No location permission is needed.").setView(entry)
            .setPositiveButton("Search",(d,w) -> {
                String query=entry.getText().toString().trim();
                if(query.length()<2 || query.length()>100) { toast("Enter a city name (2–100 characters)."); return; }
                searching=true; toast("Searching cities…");
                new Thread(() -> {
                    List<WeatherLocation> results;
                    try { results=WeatherStore.search(query); }
                    catch(Exception failure) { results=null; }
                    List<WeatherLocation> found=results;
                    runOnUiThread(() -> {
                        searching=false;
                        if(!started || isFinishing() || isDestroyed()) return;
                        if(found==null) { toast("City search failed. Check the network and try again."); return; }
                        if(found.isEmpty()) { toast("No matching city. Try a nearby city or include the country."); return; }
                        String[] labels=new String[found.size()];
                        for(int i=0;i<labels.length;i++) labels[i]=found.get(i).city;
                        new AlertDialog.Builder(this).setTitle("Select your area").setItems(labels,(dialog,i) -> saveArea(found.get(i)))
                            .setNegativeButton(android.R.string.cancel,null).show();
                    });
                },"weather-city-search").start();
            }).setNegativeButton(android.R.string.cancel,null).show();
    }
    private void useTvLocation() {
        if(checkSelfPermission(Manifest.permission.ACCESS_COARSE_LOCATION)!=PackageManager.PERMISSION_GRANTED) {
            requestPermissions(new String[]{Manifest.permission.ACCESS_COARSE_LOCATION},41); return;
        }
        if(locationListener!=null) { toast("Waiting for TV location…"); return; }
        locationManager=(LocationManager)getSystemService(LOCATION_SERVICE);
        try {
            if(locationManager==null || !locationManager.isProviderEnabled(LocationManager.NETWORK_PROVIDER)) {
                toast("This TV has no enabled network-location provider. Enable Location in TV settings or choose a city."); return;
            }
            Location last=locationManager.getLastKnownLocation(LocationManager.NETWORK_PROVIDER);
            if(last!=null && WeatherPolicy.fresh(last.getTime(),System.currentTimeMillis(),60*60*1000L)) { acceptLocation(last); return; }
            locationListener=new LocationListener() {
                @Override public void onLocationChanged(Location result) { stopLocation(); acceptLocation(result); }
                @Override public void onProviderDisabled(String provider) { stopLocation(); toast("Location disabled. Choose a city instead."); }
                @Override public void onProviderEnabled(String provider) { }
                @Override public void onStatusChanged(String provider,int state,Bundle extras) { }
            };
            locationHandler.postDelayed(locationTimeout,15_000);
            locationManager.requestSingleUpdate(LocationManager.NETWORK_PROVIDER,locationListener,android.os.Looper.getMainLooper());
            toast("Waiting for an approximate TV location…");
        } catch(SecurityException | IllegalArgumentException failure) {
            stopLocation(); toast("TV location unavailable. Choose a city instead.");
        }
    }
    private void acceptLocation(Location result) {
        try {
            if(result==null || !Double.isFinite(result.getLatitude()) || !Double.isFinite(result.getLongitude())) throw new IllegalArgumentException("Invalid location");
            // City-scale weather does not need a household's precise coordinates.
            saveArea(new WeatherLocation("Current TV area",Math.round(result.getLatitude()*100)/100.0,Math.round(result.getLongitude()*100)/100.0));
        } catch(IllegalArgumentException failure) { toast("Invalid TV location. Choose a city instead."); }
    }
    private void stopLocation() {
        locationHandler.removeCallbacks(locationTimeout);
        if(locationManager!=null && locationListener!=null) {
            try { locationManager.removeUpdates(locationListener); } catch(SecurityException ignored) { }
        }
        locationListener=null;
    }
    @Override public void onRequestPermissionsResult(int request,String[] permissions,int[] grants) {
        super.onRequestPermissionsResult(request,permissions,grants);
        if(request==41) {
            if(grants.length>0 && grants[0]==PackageManager.PERMISSION_GRANTED) useTvLocation();
            else toast("Location permission declined. You can still choose a city.");
        }
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
        started=true;
        getContentResolver().registerContentObserver(WeatherStore.URI, false, observer);
        WeatherStore.get(this).refresh(false);
        render();
    }
    @Override public void onStop() { started=false; stopLocation(); getContentResolver().unregisterContentObserver(observer); super.onStop(); }
    private String date(long millis) { return DateFormat.getDateTimeInstance(DateFormat.MEDIUM, DateFormat.SHORT).format(new Date(millis)); }
    private void render() {
        Object[] row = WeatherStore.get(this).row();
        areaTitle.setText((String)row[0]);
        areaDetails.setText(WeatherStore.get(this).location()==null ? "Allow an approximate TV location, or choose a city. Some TVs have no location provider." : "Area saved on this TV. Change it using the buttons above.");
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
