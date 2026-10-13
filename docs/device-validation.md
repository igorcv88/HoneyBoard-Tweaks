# Physical validation

No physical validation has been performed yet.

## Toolbar

- Enable module, open settings, confirm framework connection, restart keyboard.
- LSPosed logs: APK identity matched; native toolbar hook installed; refresh executed.
- Confirm four individual Select all, Copy, Undo and Redo icons in the native toolbar.
- Reorder actions with Samsung's editor, restart keyboard and confirm order persists.
- Enable Cut/Paste; disable a single action; restart and verify there are no duplicates.
- Test native Android text editor, Chrome, ChatGPT, WhatsApp and a WebView field.
- Test unsupported undo/redo, empty selection and a restricted/password field.
- Check light/dark theme, Theme Park, Keys Cafe, rotation, floating/one-handed layout,
  suggestion row, toolbar overflow and physical keyboard mode.

## Clipboard

- Use numbered, non-sensitive copies to cross each selected limit. Allow insertion to
  complete between copies. Do not benchmark using patient records or credentials.
- Confirm policy-executed marker; verify panel and DB counts agree.
- With 50 selected, copy 51 distinct entries; confirm oldest unpinned entry disappears.
- Pin an old entry; confirm it survives. Check all-pinned behavior without a crash.
- Increase to 500, then reduce to 50; confirm next new insertion trims sufficient entries.
- Verify duplicates preserve native deduplication and timestamp update behavior.
- Verify text, HTML and image/URI items, manual deletion, recent/pinned panels.
- Test separate users/Secure Folder to confirm no cross-user policy deletion.
- Test remote-origin copies with remote retention enabled/disabled.
- Observe old entries after more than one hour, overnight, keyboard restart and reboot.
  If items disappear, capture technical logs identifying the caller or native path;
  do not conclude indefinite retention from a short insertion test.

## Hidden settings and recovery

- Reveal an individual preference, enable/disable it inside native settings.
- Hide it again and verify native visibility policy resumes.
- Disable the module, restart keyboard and verify stock behavior.
- Confirm no keyboard crash if remote preferences are unavailable.
