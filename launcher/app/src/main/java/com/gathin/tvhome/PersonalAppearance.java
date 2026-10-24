package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import android.graphics.drawable.GradientDrawable;
import java.io.File;
import java.util.List;
import java.util.Random;

/** Personal, offline appearance choices. Photo files live outside the small launcher APK. */
final class PersonalAppearance {
    static final String HOME = "personal_home_background", SAVER = "personal_screensaver";
    static final String FRESH = "personal_fresh_home", IDLE = "personal_saver_minutes";
    static final String SIZING = "personal_home_photo_sizing";
    private static final int[][] COLOURS = {
            {0xFF07151C,0xFF133947,0xFF05080E}, {0xFF130C21,0xFF302344,0xFF070A13},
            {0xFF061B17,0xFF1C4034,0xFF070C12}, {0xFF21140B,0xFF493022,0xFF080A12},
            {0xFF091524,0xFF203D62,0xFF07090E}, {0xFF1F0D1A,0xFF43283F,0xFF070A12}
    };
    private PersonalAppearance() { }
    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences(Config.SETTINGS_NAME, Context.MODE_PRIVATE);
    }
    static String home(Context context) { return prefs(context).getString(HOME, "abstract"); }
    static String saver(Context context) { return prefs(context).getString(SAVER, "clock"); }
    static boolean fresh(Context context) { return prefs(context).getBoolean(FRESH, true); }
    static String sizing(Context context) { return prefs(context).getString(SIZING, "fill"); }
    static int idleMinutes(Context context) {
        int value = prefs(context).getInt(IDLE, 5);
        return value == 0 || value == 2 || value == 5 || value == 10 ? value : 5;
    }
    static List<File> photos(Context context, String group) {
        File dir = Config.dir(context);
        return WallpaperRotation.collection(WallpaperRotation.images(dir == null ? null
                : new File(dir, "wallpapers")), group);
    }
    static GradientDrawable abstractBackground(Context context) {
        SharedPreferences state = context.getSharedPreferences("personal_wallpaper_state", Context.MODE_PRIVATE);
        int previous = state.getInt("palette", -1);
        int next = previous >= 0 && previous < COLOURS.length
                ? new Random().nextInt(COLOURS.length - 1) : new Random().nextInt(COLOURS.length);
        if (previous >= 0 && previous < COLOURS.length && next >= previous) next++;
        if (!fresh(context) && previous >= 0 && previous < COLOURS.length) next = previous;
        state.edit().putInt("palette", next).apply();
        return new GradientDrawable(GradientDrawable.Orientation.TL_BR, COLOURS[next]);
    }
}
