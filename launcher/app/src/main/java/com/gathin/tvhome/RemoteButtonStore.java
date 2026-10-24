package com.gathin.tvhome;

import android.content.Context;
import android.content.SharedPreferences;
import org.json.JSONArray;
import org.json.JSONException;
import org.json.JSONObject;
import java.util.ArrayList;
import java.util.List;

/** Separate from appearance choices: changing a theme must not reset remote buttons. */
final class RemoteButtonStore {
    static final String KEY = "bindings";
    private RemoteButtonStore() { }
    static SharedPreferences prefs(Context context) {
        return context.getSharedPreferences("remote_buttons", Context.MODE_PRIVATE);
    }
    static List<RemoteButtonBinding> load(Context context) {
        List<RemoteButtonBinding> bindings = new ArrayList<>();
        try {
            JSONArray array = new JSONArray(prefs(context).getString(KEY, "[]"));
            for (int i = 0; i < Math.min(array.length(), 64); i++) {
                JSONObject item = array.optJSONObject(i);
                if (item == null) continue;
                RemoteButtonBinding binding = new RemoteButtonBinding(item.optInt("key", -1),
                        item.optInt("scan", -1), item.optString("label"), item.optString("app"));
                if (binding.valid()) bindings = RemoteButtonBinding.updated(bindings, binding);
            }
        } catch (JSONException | ClassCastException ignored) { }
        return bindings;
    }
    static boolean save(Context context, RemoteButtonBinding binding) {
        return write(context, RemoteButtonBinding.updated(load(context), binding));
    }
    static boolean remove(Context context, String id) {
        return write(context, RemoteButtonBinding.without(load(context), id));
    }
    private static boolean write(Context context, List<RemoteButtonBinding> bindings) {
        JSONArray array = new JSONArray();
        try {
            for (RemoteButtonBinding binding : bindings) {
                array.put(new JSONObject().put("key", binding.key).put("scan", binding.scan)
                        .put("label", binding.label).put("app", binding.app));
            }
        } catch (JSONException e) { return false; }
        return prefs(context).edit().putString(KEY, array.toString()).commit();
    }
}
