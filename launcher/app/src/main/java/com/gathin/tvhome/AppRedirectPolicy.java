package com.gathin.tvhome;

final class AppRedirectPolicy {
    static final String[] SOURCES = {"com.netflix.ninja", "com.google.android.youtube.tv", "com.android.vending", "com.amazon.amazonvideo.livingroom"};
    static final String[] LABELS = {"Netflix", "YouTube", "Google Play", "Prime Video"};
    private AppRedirectPolicy() { }
    static final class Gate {
        private String foreground = "", redirected = "";
        private long at;
        boolean opening(String pkg, boolean mapped, long now) {
            if (!pkg.equals(foreground)) { foreground = pkg; redirected = ""; }
            if (!mapped || (pkg.equals(redirected) && now - at < 2000)) return false;
            redirected = pkg; at = now;
            return true;
        }
    }
    static boolean source(String pkg) {
        for (String source : SOURCES) if (source.equals(pkg)) return true;
        return false;
    }
    static boolean allowed(String source, String target) {
        return source(source) && target != null && !source(target) && !"com.gathin.tvhome".equals(target)
                && !"io.freemytv".equals(target) && target.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+");
    }
}
