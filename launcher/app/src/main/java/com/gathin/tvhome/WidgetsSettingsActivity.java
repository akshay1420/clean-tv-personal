package com.gathin.tvhome;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.os.Bundle;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;

public final class WidgetsSettingsActivity extends Activity {
    private LinearLayout rows;
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(18,23,28));
        rows = new LinearLayout(this); rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(dp(48),dp(24),dp(48),dp(24)); scroll.addView(rows); setContentView(scroll);
    }
    @Override protected void onResume() { super.onResume(); render(); }
    private void row(String title, Runnable action) {
        TextView view = new TextView(this,null,0,R.style.SettingsRow);
        view.setText(title); view.setBackgroundResource(R.drawable.settings_choice_bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1,dp(56)); params.topMargin=dp(12);
        rows.addView(view,params); view.setOnClickListener(v -> action.run());
    }
    private void toggle(String key) {
        SharedPreferences prefs = PersonalAppearance.prefs(this);
        prefs.edit().putBoolean(key,!prefs.getBoolean(key,true))
                .putLong(Config.SETTINGS_REVISION_KEY,prefs.getLong(Config.SETTINGS_REVISION_KEY,0)+1).apply();
        render();
    }
    private void render() {
        rows.removeAllViews();
        TextView title=new TextView(this); title.setText(R.string.widgets); title.setTextSize(28); title.setTextColor(Color.WHITE); rows.addView(title);
        row(getString(R.string.widget_weather_state,getString(PersonalAppearance.prefs(this).getBoolean(WeatherWidget.SHOW,true)?R.string.on:R.string.off)),()->toggle(WeatherWidget.SHOW));
        row(getString(R.string.widget_clock_state,getString(PersonalAppearance.prefs(this).getBoolean(WeatherWidget.CLOCK,true)?R.string.on:R.string.off)),()->toggle(WeatherWidget.CLOCK));
        row(getString(R.string.widget_weather_details),()->WeatherWidget.openDetails(this));
        row(getString(R.string.module_cards),()-> {
            startActivity(new Intent(this,MainActivity.class).setAction(MainActivity.ACTION_MODULE_CARDS));
            finish();
        });
        TextView help=new TextView(this); help.setText(R.string.widget_help); help.setTextSize(16); help.setTextColor(0xFFB9C5CE); help.setPadding(0,dp(18),0,0); rows.addView(help);
        rows.getChildAt(1).requestFocus();
    }
}
