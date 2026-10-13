# Runtime evidence and next physical test

The supplied LSPosed export includes a modules log spanning 22:53–23:00 on
12 October 2026. The hook-debug dump contains only “No process to dump”.
The modules log establishes:

- Exact DZIF APK identity matched and all initial feature hooks installed.
- Native toolbar refresh and IME lifecycle executed.
- Select all, Undo, Redo and Copy clicks reached the module.
- The clipboard policy executed after Copy at 22:55:03.951.

It does not record whether Undo/Redo changed the editor, native row counts,
or view-model/visible tile counts. There is no HoneyBoard Tweaks hook-installation
failure in the supplied export.

## New diagnostics

Each click now has an attempt number. History routes are NATIVE_SUBMITTED,
NATIVE_BLOCKED, SHORTCUT_SUBMITTED, REJECTED or ERROR. The native route also
counts key events forwarded through the Samsung InputConnection wrapper and
how many were accepted. Neither count is proof of an editor mutation.
`editor_effect=unverified` is intentional.

Native DZIF Undo/Redo use the same gesture action classes as Keys Cafe:
`fa0.l` and `fa0.j`. The native actions clear engine context and forward
Ctrl+Z / Ctrl+Y. Their eligibility guard is retained. The compatibility option
sends editor shortcuts directly, with an optional Ctrl+Shift+Z redo.

Fallback is allowed only if native preparation fails before dispatch. An exception
after native execution begins does not trigger another Undo/Redo.
Toolbar callback order is preserved; callback exceptions are contained without
repeating the action. Select all/Copy remain context-menu commands.

Android's InputConnection documentation does not promise Undo/Redo context-menu
support. However, AOSP TextView handles those IDs and returns true even if its
editor undoes nothing. Thus the identifier alone does not establish the failure.

## Physical test

1. Install a build from this PR, enable the module, restart Samsung Keyboard.
2. In a native Android text field, type a short phrase and a final space.
3. Undo once, verify the text yourself, then Redo once.
4. Export the LSPosed modules log, including the repeated action lines.
5. If native reports NATIVE_BLOCKED, finish the composing word and repeat.
6. For an editor that ignores the native route, enable the shortcut option and
   test once. Change redo between Ctrl+Y and Ctrl+Shift+Z manually; the module
   never sends both automatically.
7. Repeat in Chrome/WebView and WhatsApp; support must be assessed per editor.

Logcat is now mirrored under the module's own tag and can be exported from Termux:

```sh
su -c 'logcat -d -v time -s HoneyBoardTweaks:I "*:S"' > /sdcard/Download/hbt-runtime-log.txt
```

## Clipboard test

Use unique, non-sensitive numbered text entries. With capacity 50, insert 51
entries, allowing each native insertion to finish. Repeat for 100, 150, 200, 500.
Open the clipboard after copying; compare:

- `clipboard inserted ... db_count=...`: rows after native insertion.
- `clipboard viewmodel_count=...`: count returned to the panel model.
- What you see in the native panel: rendering remains physically unverified.

View-model logging is coalesced when the count stays unchanged. Pinned entries
count toward capacity and survive trimming. If pins alone fill/exceed capacity,
the limit is soft. Duplicate copies follow native deduplication.

Eviction now reports planned/completed deletions and stops on failure.
Native store deletion removes media files before its row operation, so a SQL
transaction alone cannot restore deleted media. This patch does not claim atomic
rollback. After a deletion attempt it does not replay the stock trim policy;
the new insert can continue and temporarily exceed the chosen capacity.

Clipboard search and enlarged key previews are still unimplemented. No release
workflow is dispatched by this patch; the repository retains its manual-only
release policy.
