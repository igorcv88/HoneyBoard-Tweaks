package io.github.igorcv88.honeyboardtweaks;

import android.content.SharedPreferences;
import java.util.ArrayList;
import java.util.List;

final class Config {
    static final String TARGET = "com.samsung.android.honeyboard";
    static final String GROUP = "tweaks";
    static final int[] CAPACITIES = {40, 50, 100, 150, 200, 500};
    static final String[] HIDDEN_KEYS = {
        "SETTINGS_SHOW_SMS_OTP", "SETTINGS_SPECIFIC_ASSIST", "SETTINGS_TOUCH_EVENT_RECORD",
        "SETTINGS_WRITING_ASSIST", "SETTINGS_DRAWING_ASSIST", "SETTINGS_WRITING_ASSIST_TRANSLATION",
        "SETTINGS_VOICE_INPUT", "SETTINGS_DEFAULT_HWR_ON", "settings_direct_writing",
        "SETTINGS_SAVE_SCREENSHOTS_TO_CLIPBOARD", "SETTINGS_PHYSICAL_KEYBOARD_TOOLBAR",
        "SETTINGS_SHOW_BUTTON_TO_HIDE_KEYBOARD_RELATIVE_LINK", "japanese_input_options",
        "enhanced_prediction", "selected_language_download_cue"
    };
    static boolean enabled(SharedPreferences p, String key, boolean fallback) {
        return p != null && p.getBoolean(key, fallback);
    }
    static List<EditAction> actions(SharedPreferences p) {
        List<EditAction> result = new ArrayList<>();
        if (!enabled(p, "toolbar", true)) return result;
        for (EditAction a : EditAction.values()) {
            if (enabled(p, "action_" + a.key, a.defaultEnabled)) result.add(a);
        }
        return result;
    }
    static int capacity(SharedPreferences p) {
        return ClipboardPolicy.validCapacity(p == null ? 40 : p.getInt("capacity", 100));
    }
    static boolean showSetting(SharedPreferences p, String key) {
        for (String known : HIDDEN_KEYS) {
            if (known.equals(key)) return enabled(p, "show_" + key, false);
        }
        if ("SETTINGS_TOUCH_EVENT_RECORD_CATEGORY".equals(key))
            return enabled(p, "show_SETTINGS_TOUCH_EVENT_RECORD", false);
        if ("SETTINGS_CATEGORY_VOICE_INPUT".equals(key)) {
            for (String child : new String[]{"SETTINGS_VOICE_INPUT", "SETTINGS_DEFAULT_HWR_ON",
                    "settings_direct_writing", "SETTINGS_SAVE_SCREENSHOTS_TO_CLIPBOARD",
                    "SETTINGS_WRITING_ASSIST_TRANSLATION"})
                if (enabled(p, "show_" + child, false)) return true;
        }
        if ("SETTINGS_SHOW_BUTTON_TO_HIDE_KEYBOARD_RELATIVE_LINK_CATEGORY".equals(key))
            return enabled(p, "show_SETTINGS_SHOW_BUTTON_TO_HIDE_KEYBOARD_RELATIVE_LINK", false);
        if ("regional_language_settings".equals(key)) {
            for (String child : new String[]{"japanese_input_options", "enhanced_prediction",
                    "selected_language_download_cue"})
                if (enabled(p, "show_" + child, false)) return true;
        }
        return false;
    }
}
