package com.gathin.tvhome;

import android.app.Activity;
import android.app.AlertDialog;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.provider.Settings;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import java.io.BufferedReader;
import java.io.InputStreamReader;

public final class AppearanceSettingsActivity extends Activity {
    private LinearLayout rows;
    private final String[] homes = {"abstract", "nature", "world", "nightlife", "none", "theme"};
    private final String[] savers = {"clock", "clock_nature", "nature", "world", "nightlife"};
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(18, 23, 28));
        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(dp(48), dp(24), dp(48), dp(24));
        scroll.addView(rows);
        setContentView(scroll);
    }
    @Override protected void onResume() { super.onResume(); render(); }
    private void row(String label, Runnable action) {
        TextView row = new TextView(this, null, 0, R.style.SettingsRow);
        row.setText(label);
        row.setBackgroundResource(R.drawable.settings_choice_bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.topMargin = dp(10);
        rows.addView(row, params);
        row.setOnClickListener(v -> action.run());
    }
    private String[] homeLabels() { return getResources().getStringArray(R.array.personal_home_labels); }
    private String[] saverLabels() { return getResources().getStringArray(R.array.personal_saver_labels); }
    private static int index(String[] values, String selected) {
        for (int i = 0; i < values.length; i++) if (values[i].equals(selected)) return i;
        return 0;
    }
    private void save(String key, String value) {
        PersonalAppearance.prefs(this).edit().putString(key, value).apply();
        changed(); render();
    }
    private void changed() {
        SharedPreferences prefs = PersonalAppearance.prefs(this);
        prefs.edit().putLong(Config.SETTINGS_REVISION_KEY,
                prefs.getLong(Config.SETTINGS_REVISION_KEY, 0) + 1).apply();
    }
    private void render() {
        rows.removeAllViews();
        TextView title = new TextView(this);
        title.setText(R.string.personal_appearance);
        title.setTextColor(Color.WHITE); title.setTextSize(28);
        rows.addView(title);
        row(getString(R.string.personal_home_state, homeLabels()[index(homes, PersonalAppearance.home(this))]), () ->
                new AlertDialog.Builder(this).setTitle(R.string.personal_home)
                        .setSingleChoiceItems(homeLabels(), index(homes, PersonalAppearance.home(this)), (d, i) -> {
                            d.dismiss(); save(PersonalAppearance.HOME, homes[i]);
                        }).setNegativeButton(android.R.string.cancel, null).show());
        String[] sizingLabels = getResources().getStringArray(R.array.personal_sizing_labels);
        int sizingIndex = "fit".equals(PersonalAppearance.sizing(this)) ? 1 : 0;
        row(getString(R.string.personal_sizing_state, sizingLabels[sizingIndex]), () ->
                new AlertDialog.Builder(this).setTitle(R.string.personal_sizing)
                        .setSingleChoiceItems(sizingLabels, sizingIndex, (d, i) -> {
                            d.dismiss(); save(PersonalAppearance.SIZING, i == 1 ? "fit" : "fill");
                        }).setNegativeButton(android.R.string.cancel, null).show());
        row(getString(R.string.personal_fresh_state, getString(PersonalAppearance.fresh(this) ? R.string.on : R.string.off)), () -> {
            PersonalAppearance.prefs(this).edit().putBoolean(PersonalAppearance.FRESH, !PersonalAppearance.fresh(this)).apply();
            changed(); render();
        });
        row(getString(R.string.personal_saver_state, saverLabels()[index(savers, PersonalAppearance.saver(this))]), () ->
                new AlertDialog.Builder(this).setTitle(R.string.personal_saver)
                        .setSingleChoiceItems(saverLabels(), index(savers, PersonalAppearance.saver(this)), (d, i) -> {
                            d.dismiss(); save(PersonalAppearance.SAVER, savers[i]);
                        }).setNegativeButton(android.R.string.cancel, null).show());
        int minutes = PersonalAppearance.idleMinutes(this);
        row(getString(R.string.personal_idle_state, minutes == 0 ? getString(R.string.off) : minutes + " min"), () -> {
            int[] values = {0, 2, 5, 10};
            String[] labels = {getString(R.string.off), "2 min", "5 min", "10 min"};
            new AlertDialog.Builder(this).setTitle(R.string.personal_idle)
                    .setItems(labels, (d, i) -> {
                        PersonalAppearance.prefs(this).edit().putInt(PersonalAppearance.IDLE, values[i]).apply();
                        changed(); render();
                    }).setNegativeButton(android.R.string.cancel, null).show();
        });
        row(getString(R.string.personal_preview), () -> startActivity(new Intent(this, ClockActivity.class)));
        row(getString(R.string.personal_system_saver), () -> {
            try { startActivity(new Intent(Settings.ACTION_DREAM_SETTINGS)); }
            catch (android.content.ActivityNotFoundException | SecurityException e) { }
        });
        row(getString(R.string.personal_credits), this::credits);
        TextView info = new TextView(this);
        info.setText(getString(R.string.personal_collection_info, PersonalAppearance.photos(this,"nature").size(),
                PersonalAppearance.photos(this,"world").size(), PersonalAppearance.photos(this,"nightlife").size()));
        info.setTextColor(0xFFB9C5CE); info.setTextSize(16); info.setPadding(0,dp(18),0,dp(8));
        rows.addView(info);
        rows.getChildAt(1).requestFocus();
    }
    private void credits() {
        StringBuilder text = new StringBuilder();
        try (BufferedReader reader = new BufferedReader(new InputStreamReader(getAssets().open("photo-credits.txt"), java.nio.charset.StandardCharsets.UTF_8))) {
            String line;
            while ((line = reader.readLine()) != null) text.append(line).append('\n');
        } catch (java.io.IOException e) { text.append(getString(R.string.not_available)); }
        new AlertDialog.Builder(this).setTitle(R.string.personal_credits).setMessage(text.toString())
                .setPositiveButton(android.R.string.ok, null).show();
    }
}
