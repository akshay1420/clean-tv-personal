package com.gathin.tvweather;

import android.content.ContentProvider;
import android.content.ContentValues;
import android.database.Cursor;
import android.database.MatrixCursor;
import android.net.Uri;

public final class WeatherProvider extends ContentProvider {
    private static final String[] COLUMNS = { "city", "summary", "observed", "updated", "loading", "error",
        "aqi_summary", "aqi_observed", "aqi_updated", "aqi_error" };
    @Override public boolean onCreate() { WeatherStore.get(getContext()); return true; }
    private void check(Uri uri) {
        if (!WeatherStore.URI.equals(uri)) throw new IllegalArgumentException("Unsupported weather URI");
    }
    @Override public Cursor query(Uri uri, String[] projection, String selection, String[] args, String sort) {
        check(uri);
        if (selection != null || args != null || sort != null) throw new IllegalArgumentException("Filtering unsupported");
        WeatherStore store = WeatherStore.get(getContext());
        store.refresh(false);
        Object[] values = store.row();
        String[] columns = projection == null ? COLUMNS : projection;
        Object[] result = new Object[columns.length];
        for (int i = 0; i < columns.length; i++) {
            int index = java.util.Arrays.asList(COLUMNS).indexOf(columns[i]);
            if (index < 0) throw new IllegalArgumentException("Unknown weather column");
            result[i] = values[index];
        }
        MatrixCursor cursor = new MatrixCursor(columns, 1);
        cursor.addRow(result);
        cursor.setNotificationUri(getContext().getContentResolver(), WeatherStore.URI);
        return cursor;
    }
    @Override public String getType(Uri uri) { check(uri); return "vnd.android.cursor.item/vnd.com.gathin.tvweather.current"; }
    @Override public Uri insert(Uri uri, ContentValues values) { throw new UnsupportedOperationException("Read only"); }
    @Override public int update(Uri uri, ContentValues values, String selection, String[] args) { throw new UnsupportedOperationException("Read only"); }
    @Override public int delete(Uri uri, String selection, String[] args) { throw new UnsupportedOperationException("Read only"); }
}
