package io.github.igorcv88.honeyboardtweaks;

import android.app.Activity;
import android.content.Intent;
import android.content.SharedPreferences;
import android.graphics.Color;
import android.graphics.drawable.GradientDrawable;
import android.os.Bundle;
import android.view.View;
import android.widget.Button;
import android.widget.LinearLayout;
import android.widget.ScrollView;
import android.widget.Spinner;
import android.widget.ArrayAdapter;
import android.widget.AdapterView;
import android.widget.Switch;
import android.widget.TextView;

public final class MainActivity extends Activity {
    private App app;
    private LinearLayout body;
    private boolean rendering;
    @Override public void onCreate(Bundle state) {
        super.onCreate(state); app = (App) getApplication();
    }
    @Override public void onResume() {
        super.onResume(); app.screen = new java.lang.ref.WeakReference<>(this); render();
    }
    @Override public void onPause() {
        if (app.screen.get() == this) app.screen.clear();
        super.onPause();
    }
    void render() {
        rendering = true;
        ScrollView scroll = new ScrollView(this);
        body = new LinearLayout(this); body.setOrientation(LinearLayout.VERTICAL);
        body.setPadding(dp(24), dp(48), dp(24), dp(32)); scroll.addView(body);
        scroll.setBackgroundColor(Color.rgb(18, 20, 25)); setContentView(scroll);
        text(body, "HoneyBoard Tweaks", 28, Color.WHITE);
        text(body, "Samsung Keyboard · native toolbar", 14, 0xffabb4c4);
        text(body, app.prefs != null ? "Framework connected · API 102" :
                "Enable this module in LSPosed with Samsung Keyboard selected, then reopen this app.",
                14, app.prefs != null ? 0xff91d5b7 : 0xffffc989);
        LinearLayout toolbar = card("Toolbar");
        text(toolbar, "Changes appear after restarting Samsung Keyboard. Arrange actions using the keyboard’s own toolbar editor.",
                14, 0xffabb4c4);
        toggle(toolbar, "Enable toolbar actions", "toolbar", true);
        for (EditAction action : EditAction.values())
            toggle(toolbar, action.label, "action_" + action.key, action.defaultEnabled);
        text(toolbar, "Undo and redo depend on the current app’s editor.", 13, 0xffabb4c4);
        LinearLayout clipboard = card("Clipboard");
        toggle(clipboard, "Extend native clipboard history", "clipboard", true);
        text(clipboard, "History capacity", 16, Color.WHITE);
        Spinner capacity = new Spinner(this);
        String[] labels = {"40", "50", "100", "150", "200", "500"};
        capacity.setAdapter(new ArrayAdapter<>(this, android.R.layout.simple_spinner_dropdown_item, labels));
        int current = app.prefs == null ? 100 : app.prefs.getInt("capacity", 100);
        int selected = 2;
        for (int i = 0; i < Config.CAPACITIES.length; i++)
            if (Config.CAPACITIES[i] == current) selected = i;
        capacity.setSelection(selected, false); clipboard.addView(capacity);
        capacity.setEnabled(app.prefs != null);
        capacity.setOnItemSelectedListener(new AdapterView.OnItemSelectedListener() {
            @Override public void onItemSelected(AdapterView<?> parent, View view, int position, long id) {
                int choice = Config.CAPACITIES[position];
                if (!rendering && app.prefs != null && app.prefs.getInt("capacity", 100) != choice)
                    write(p -> p.putInt("capacity", choice));
            }
            @Override public void onNothingSelected(AdapterView<?> parent) {}
        });
        toggle(clipboard, "Keep remote copy history", "keep_history", true);
        text(clipboard, "History has no added time expiry. Keep remote copies instead of replacing the previous remote item. Old unpinned items are removed only at the capacity limit; pinned items stay. Manual deletion remains available.",
                13, 0xffabb4c4);
        text(clipboard, "Android’s last copied item is separate. Its system expiry is unchanged.", 13, 0xffabb4c4);
        LinearLayout hidden = card("Hidden keyboard settings");
        text(hidden, "Show individual options in Samsung Keyboard settings. Use the native switch there to enable or disable the feature. Visibility does not guarantee backend support.",
                13, 0xffabb4c4);
        String[] names = {"SMS one-time codes", "Specific assist", "Touch event recording", "Writing assist",
                "Drawing assist", "Writing assist translation", "Voice input", "Default handwriting",
                "Direct writing", "Save screenshots to clipboard", "Physical keyboard toolbar",
                "Hide keyboard button link", "Japanese input options", "Enhanced prediction",
                "Language download cue"};
        for (int i = 0; i < Config.HIDDEN_KEYS.length; i++)
            toggle(hidden, names[i], "show_" + Config.HIDDEN_KEYS[i], false);
        Button settings = new Button(this); settings.setText("Open Samsung Keyboard settings");
        settings.setOnClickListener(v -> {
            try { startActivity(new Intent("android.settings.INPUT_METHOD_SETTINGS")); }
            catch (RuntimeException error) { android.widget.Toast.makeText(this,
                    "Keyboard settings unavailable", android.widget.Toast.LENGTH_SHORT).show(); }
        }); hidden.addView(settings);
        LinearLayout compatibility = card("Compatibility");
        String installed = "Not installed";
        try { installed = getPackageManager().getPackageInfo(Config.TARGET, 0).versionName; }
        catch (android.content.pm.PackageManager.NameNotFoundException ignored) {}
        text(compatibility, "Installed keyboard: " + installed +
                "\nMapped APK: 5.9.40.75 · DZIF\nExact APK SHA-256 gate\nOnly Samsung Keyboard is hooked" +
                "\nDevice validation pending. LSPosed logs distinguish hook installation from execution.",
                13, 0xffabb4c4);
        rendering = false;
    }
    private LinearLayout card(String title) {
        LinearLayout card = new LinearLayout(this); card.setOrientation(LinearLayout.VERTICAL);
        card.setPadding(dp(18), dp(16), dp(18), dp(16));
        GradientDrawable background = new GradientDrawable();
        background.setColor(0xff222630); background.setCornerRadius(dp(18)); card.setBackground(background);
        LinearLayout.LayoutParams params = new LinearLayout.LayoutParams(-1, -2);
        params.topMargin = dp(18); body.addView(card, params);
        text(card, title, 20, Color.WHITE); return card;
    }
    private void toggle(LinearLayout parent, String label, String key, boolean fallback) {
        Switch control = new Switch(this); control.setText(label); control.setTextColor(Color.WHITE);
        control.setPadding(0, dp(10), 0, dp(10));
        control.setChecked(app.prefs == null ? fallback : app.prefs.getBoolean(key, fallback));
        control.setEnabled(app.prefs != null); parent.addView(control);
        control.setOnCheckedChangeListener((button, checked) -> {
            if (!rendering) write(editor -> editor.putBoolean(key, checked));
        });
    }
    interface Change { void apply(SharedPreferences.Editor editor); }
    private void write(Change change) {
        try {
            if (app.prefs == null) throw new IllegalStateException();
            SharedPreferences.Editor editor = app.prefs.edit(); change.apply(editor);
            if (!editor.commit()) throw new IllegalStateException();
        } catch (RuntimeException error) {
            android.widget.Toast.makeText(this, "Could not save. Reconnect LSPosed and retry.",
                    android.widget.Toast.LENGTH_LONG).show();
            render();
        }
    }
    private void text(LinearLayout parent, String value, int size, int color) {
        TextView text = new TextView(this); text.setText(value); text.setTextSize(size);
        text.setTextColor(color); text.setPadding(0, 0, 0, dp(10)); parent.addView(text);
    }
    private int dp(int value) { return Math.round(value * getResources().getDisplayMetrics().density); }
}
