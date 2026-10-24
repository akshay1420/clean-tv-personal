package com.gathin.tvhome;

import java.util.ArrayList;
import java.util.List;

/** User app-button mappings. Essential controls and game controllers always pass through. */
final class RemoteButtonBinding {
    final int key, scan;
    final String label, app;

    RemoteButtonBinding(int key, int scan, String label, String app) {
        this.key = key; this.scan = scan; this.label = label; this.app = app;
    }

    static boolean assignable(int key, int source) {
        if ((source & 0x401) == 0x401 || (source & 0x1000010) == 0x1000010) return false;
        if (key <= 0 || key > 65535) return false;
        switch (key) {
            case 1: case 2: case 3: case 4: case 5: case 6:
            case 19: case 20: case 21: case 22: case 23: case 24: case 25: case 26:
            case 61: case 62: case 66: case 67: case 91: case 111: case 112:
            case 122: case 123: case 160: case 164: case 170: case 176: case 177:
            case 178: case 179: case 181: case 187: case 219: case 220: case 221: case 223: case 224:
            case 231: case 264: case 268: case 269: case 270: case 271: case 276: case 284:
            case 280: case 281: case 282: case 283:
                return false;
            default: return true;
        }
    }

    boolean valid() {
        return assignable(key, 0) && scan >= 0 && scan <= 65535
                && label != null && !label.trim().isEmpty() && label.length() <= 80
                && app != null && app.matches("[A-Za-z][A-Za-z0-9_]*(\\.[A-Za-z0-9_]+)+");
    }

    String id() { return key + ":" + scan; }
    boolean matches(int code, int scancode) { return key == code && (scan == 0 || scan == scancode); }

    static RemoteButtonBinding find(List<RemoteButtonBinding> bindings, int key, int scan, int source) {
        if (!assignable(key, source)) return null;
        RemoteButtonBinding generic = null;
        for (RemoteButtonBinding binding : bindings) {
            if (!binding.valid() || !binding.matches(key, scan)) continue;
            if (binding.scan != 0) return binding;
            generic = binding;
        }
        return generic;
    }

    static List<RemoteButtonBinding> without(List<RemoteButtonBinding> bindings, String id) {
        List<RemoteButtonBinding> result = new ArrayList<>();
        for (RemoteButtonBinding binding : bindings) if (!binding.id().equals(id)) result.add(binding);
        return result;
    }

    static List<RemoteButtonBinding> updated(List<RemoteButtonBinding> bindings, RemoteButtonBinding binding) {
        if (!binding.valid()) throw new IllegalArgumentException("Invalid remote button");
        List<RemoteButtonBinding> result = without(bindings, binding.id());
        result.add(binding);
        return result;
    }
}
