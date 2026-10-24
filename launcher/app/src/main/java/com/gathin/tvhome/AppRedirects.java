package com.gathin.tvhome;

import android.content.Context;
import java.util.HashMap;
import java.util.Map;

/** Explicitly selected fallback for firmware-handled buttons; also redirects manual app openings. */
final class AppRedirects {
    static final String PREFIX = "redirect.";
    private AppRedirects() { }
    static Map<String,String> load(Context context) {
        Map<String,String> result = new HashMap<>();
        for (String source : AppRedirectPolicy.SOURCES) {
            String target = RemoteButtonStore.prefs(context).getString(PREFIX + source, "");
            if (AppRedirectPolicy.allowed(source,target)) result.put(source,target);
        }
        return result;
    }
    static boolean save(Context context, String source, String target) {
        if (!AppRedirectPolicy.source(source)) return false;
        if (target == null) return RemoteButtonStore.prefs(context).edit().remove(PREFIX+source).commit();
        return AppRedirectPolicy.allowed(source,target)
                && RemoteButtonStore.prefs(context).edit().putString(PREFIX+source,target).commit();
    }
}
