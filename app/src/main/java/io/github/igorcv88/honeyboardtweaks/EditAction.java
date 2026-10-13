package io.github.igorcv88.honeyboardtweaks;

import android.view.inputmethod.InputConnection;

enum EditAction {
    SELECT_ALL("select_all", "Select all", android.R.id.selectAll, true),
    COPY("copy", "Copy", android.R.id.copy, true),
    UNDO("undo", "Undo", android.R.id.undo, true),
    REDO("redo", "Redo", android.R.id.redo, true),
    CUT("cut", "Cut", android.R.id.cut, false),
    PASTE("paste", "Paste", android.R.id.paste, false);

    final String key, label, beeId;
    final int command;
    final boolean defaultEnabled;
    EditAction(String key, String label, int command, boolean defaultEnabled) {
        this.key = key; this.label = label; this.command = command;
        this.defaultEnabled = defaultEnabled; this.beeId = "hbt_" + key;
    }
    boolean execute(InputConnection connection) {
        return connection != null && connection.performContextMenuAction(command);
    }
}
