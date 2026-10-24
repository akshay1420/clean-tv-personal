package com.gathin.tvhome;

import android.accessibilityservice.AccessibilityServiceInfo;
import android.app.Activity;
import android.app.AlertDialog;
import android.content.ActivityNotFoundException;
import android.content.Intent;
import android.content.pm.PackageManager;
import android.content.pm.ResolveInfo;
import android.graphics.Color;
import android.os.Bundle;
import android.os.Handler;
import android.os.Looper;
import android.provider.Settings;
import android.view.KeyEvent;
import android.view.View;
import android.view.accessibility.AccessibilityManager;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.TextView;
import android.widget.Toast;
import java.util.ArrayList;
import java.util.Arrays;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.function.Consumer;

/** On-TV app-button learning and editing. Uses the existing, explicitly enabled key helper. */
public final class RemoteButtonsActivity extends Activity {
    private LinearLayout rows;
    private boolean learning;
    private String learningLabel;
    private final Handler handler = new Handler(Looper.getMainLooper());
    private final Runnable timeout = () -> {
        cancelLearning();
        render();
        Toast.makeText(this, R.string.remote_timeout, Toast.LENGTH_LONG).show();
    };
    private final RemoteButtonLearning.Listener listener = (key, scan) -> {
        if (!learning || isFinishing()) return;
        String label = learningLabel;
        cancelLearning();
        render();
        pickApp(new RemoteButtonBinding(key, scan, label, "placeholder.app"));
    };

    @Override public void onCreate(Bundle state) {
        super.onCreate(state);
        ScrollView scroll = new ScrollView(this);
        scroll.setBackgroundColor(Color.rgb(18, 23, 28));
        scroll.setFillViewport(true);
        rows = new LinearLayout(this);
        rows.setOrientation(LinearLayout.VERTICAL);
        rows.setPadding(dp(48), dp(24), dp(48), dp(24));
        scroll.addView(rows);
        setContentView(scroll);
    }

    @Override protected void onResume() { super.onResume(); render(); }
    @Override protected void onPause() { cancelLearning(); super.onPause(); }

    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }

    private void text(String value, int size) {
        TextView text = new TextView(this);
        text.setText(value);
        text.setTextColor(Color.WHITE);
        text.setTextSize(size);
        text.setPadding(0, dp(8), 0, dp(12));
        rows.addView(text);
    }

    private void row(String value, Runnable action) {
        TextView row = new TextView(this, null, 0, R.style.SettingsRow);
        row.setText(value);
        row.setBackgroundResource(R.drawable.settings_choice_bg);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, dp(56));
        params.topMargin = dp(10);
        rows.addView(row, params);
        row.setOnClickListener(v -> action.run());
    }

    private void render() {
        rows.removeAllViews();
        text(getString(R.string.remote_buttons), 28);
        if (learning) {
            text(getString(R.string.remote_press_button, learningLabel), 21);
            row(getString(android.R.string.cancel), () -> { cancelLearning(); render(); });
        } else {
            boolean enabled = helperEnabled();
            text(getString(enabled ? R.string.remote_enabled : R.string.remote_disabled), 18);
            row(getString(R.string.remote_enable), this::openHelperSettings);
            text(getString(R.string.remote_redirect_help), 16);
            Map<String,String> redirects = AppRedirects.load(this);
            for (int i = 0; i < AppRedirectPolicy.SOURCES.length; i++) {
                String source = AppRedirectPolicy.SOURCES[i], label = AppRedirectPolicy.LABELS[i];
                String target = redirects.get(source);
                row(label + " → " + (target == null ? getString(R.string.remote_original) : appLabel(target)),
                        () -> pickRedirect(source,label));
            }
            row(getString(R.string.remote_add), this::chooseLabel);
            List<RemoteButtonBinding> bindings = RemoteButtonStore.load(this);
            if (bindings.isEmpty()) text(getString(R.string.remote_empty), 18);
            for (RemoteButtonBinding binding : bindings) {
                row(binding.label + " → " + appLabel(binding.app), () -> edit(binding));
            }
            text(getString(R.string.remote_help), 16);
        }
        for (int i = 0; i < rows.getChildCount(); i++) {
            View child = rows.getChildAt(i);
            if (child.isFocusable()) { child.requestFocus(); break; }
        }
    }

    private boolean helperEnabled() {
        AccessibilityManager manager = getSystemService(AccessibilityManager.class);
        if (manager == null) return false;
        for (AccessibilityServiceInfo info : manager.getEnabledAccessibilityServiceList(
                AccessibilityServiceInfo.FEEDBACK_ALL_MASK)) {
            android.content.pm.ServiceInfo service = info.getResolveInfo().serviceInfo;
            if (getPackageName().equals(service.packageName)
                    && InputKeyService.class.getName().equals(service.name)) return true;
        }
        return false;
    }

    private void openHelperSettings() {
        try {
            startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS));
        } catch (ActivityNotFoundException | SecurityException e) {
            try { startActivity(new Intent(Settings.ACTION_ACCESSIBILITY_SETTINGS)); }
            catch (ActivityNotFoundException | SecurityException ignored) {
                Toast.makeText(this, R.string.not_available, Toast.LENGTH_LONG).show();
            }
        }
    }

    private void chooseLabel() {
        if (!helperEnabled()) { openHelperSettings(); return; }
        String[] labels = {"Netflix", "YouTube", "Google Play", "Prime Video", getString(R.string.remote_other)};
        new AlertDialog.Builder(this).setTitle(R.string.remote_choose_button)
                .setItems(labels, (dialog, which) -> {
                    if (which < 4) {
                        pickRedirect(AppRedirectPolicy.SOURCES[which],labels[which]);
                        return;
                    }
                    learningLabel = labels[which]; learning = true;
                    RemoteButtonLearning.begin(listener);
                    handler.postDelayed(timeout, 20_000);
                    render();
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void cancelLearning() {
        learning = false;
        RemoteButtonLearning.cancel(listener);
        handler.removeCallbacks(timeout);
    }

    @Override public boolean dispatchKeyEvent(KeyEvent event) {
        if (learning && event.getKeyCode() == KeyEvent.KEYCODE_BACK) {
            if (event.getAction() == KeyEvent.ACTION_UP) { cancelLearning(); render(); }
            return true;
        }
        return RemoteButtonLearning.consume(event) || super.dispatchKeyEvent(event);
    }

    private String appLabel(String pkg) {
        try { return getPackageManager().getApplicationLabel(getPackageManager().getApplicationInfo(pkg, 0)).toString(); }
        catch (PackageManager.NameNotFoundException e) { return pkg + " " + getString(R.string.remote_missing); }
    }

    private void edit(RemoteButtonBinding binding) {
        new AlertDialog.Builder(this).setTitle(binding.label)
                .setItems(new String[]{getString(R.string.remote_change), getString(R.string.remote_restore)},
                        (dialog, which) -> {
                            if (which == 0) pickApp(binding);
                            else {
                                boolean saved = RemoteButtonStore.remove(this, binding.id());
                                report(saved); render();
                            }
                        }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void pickApp(RemoteButtonBinding binding) {
        pickApp(binding.label, false, pkg -> {
            report(RemoteButtonStore.save(this, new RemoteButtonBinding(binding.key, binding.scan,binding.label,pkg)));
            render();
        });
    }

    private void pickRedirect(String source, String label) {
        if (!helperEnabled()) { openHelperSettings(); return; }
        new AlertDialog.Builder(this).setTitle(label).setMessage(R.string.remote_redirect_confirm)
                .setPositiveButton(R.string.remote_change, (d,w) -> pickApp(label,true,pkg -> {
                    report(AppRedirects.save(this,source,pkg)); render();
                })).setNeutralButton(R.string.remote_restore,(d,w) -> {
                    report(AppRedirects.save(this,source,null)); render();
                }).setNegativeButton(android.R.string.cancel,null).show();
    }

    private void pickApp(String label, boolean redirect, Consumer<String> chosen) {
        PackageManager pm = getPackageManager();
        Map<String, String> apps = new LinkedHashMap<>();
        Intent query = new Intent(Intent.ACTION_MAIN).addCategory(Intent.CATEGORY_LEANBACK_LAUNCHER);
        for (ResolveInfo info : pm.queryIntentActivities(query, 0)) {
            String pkg = info.activityInfo.packageName;
            if (!pkg.equals(getPackageName()) && !pkg.equals("io.freemytv")
                    && (!redirect || !AppRedirectPolicy.source(pkg))) apps.put(pkg, info.loadLabel(pm).toString());
        }
        List<String> packages = new ArrayList<>(apps.keySet());
        packages.sort((a, b) -> {
            return apps.get(a).compareToIgnoreCase(apps.get(b));
        });
        String[] labels = new String[packages.size()];
        for (int i = 0; i < labels.length; i++) labels[i] = apps.get(packages.get(i));
        new AlertDialog.Builder(this).setTitle(getString(R.string.remote_select_app, label))
                .setItems(labels, (dialog, which) -> {
                    chosen.accept(packages.get(which));
                }).setNegativeButton(android.R.string.cancel, null).show();
    }

    private void report(boolean saved) {
        Toast.makeText(this, saved ? R.string.remote_saved : R.string.remote_save_failed, Toast.LENGTH_SHORT).show();
    }
}
