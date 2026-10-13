package io.github.igorcv88.honeyboardtweaks;

import android.view.inputmethod.InputConnection;
import android.view.KeyEvent;
import android.os.SystemClock;

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
        if (this == UNDO || this == REDO) return sendShortcut(connection, false);
        return connection != null && connection.performContextMenuAction(command);
    }
    boolean sendShortcut(InputConnection connection, boolean shiftRedo) {
        if (connection == null) return false;
        int code = this == UNDO || shiftRedo ? KeyEvent.KEYCODE_Z : KeyEvent.KEYCODE_Y;
        int meta = KeyEvent.META_CTRL_ON | (this == REDO && shiftRedo ? KeyEvent.META_SHIFT_ON : 0);
        long downTime = SystemClock.uptimeMillis();
        boolean down;
        boolean up;
        try {
            down = connection.sendKeyEvent(new KeyEvent(downTime, downTime,
                    KeyEvent.ACTION_DOWN, code, 0, meta, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
        } finally {
            // Release even after a rejected or partially delivered key-down.
            up = connection.sendKeyEvent(new KeyEvent(downTime, SystemClock.uptimeMillis(),
                    KeyEvent.ACTION_UP, code, 0, meta, 0, 0,
                    KeyEvent.FLAG_SOFT_KEYBOARD | KeyEvent.FLAG_KEEP_TOUCH_MODE));
        }
        return down && up;
    }
}
