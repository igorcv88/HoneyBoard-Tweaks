# HoneyBoard Tweaks

LSPosed module for Samsung Keyboard on the Galaxy S25 Ultra DZIF build.

Select all, Copy, Undo and Redo are separate native toolbar items, enabled by default.
Cut and Paste can be enabled individually. Each action executes directly in the current
editor; there is no intermediate editing board or additional keyboard row.
Samsung renders the icons and owns toolbar customization, layout and overflow.
Arrange the buttons using the native toolbar editor. Existing saved action order is respected.

## Clipboard

Configure native history capacity: 40, **50, 100, 150, 200 or 500**; default 100.
The adapter replaces the identified pre-insert trimming policy and uses Samsung's own store
to remove old items and associated media. It preserves pinned items and separates users.
If all available victims are pinned, the limit is soft. Reducing capacity takes effect on the next
new nonduplicate insertion and removes enough oldest unpinned items to reach the new limit.

The Honeyboard database and Android's primary clipboard are different. In the analyzed APK,
the local insertion policy has no timestamp-based expiry. The module keeps that behavior
and can retain origin 1/2 remote copies that Samsung otherwise replaces with the next copy.
**This is not a hook disabling Android/system-server primary-clipboard expiry.** External
clear commands, remote revocation and manual deletion remain functional; indefinite retention
against every external deletion source has not been established. The settings label is
“Keep remote copy history” to describe the implemented behavior precisely.

## Hidden settings

Individual controls reveal OTP suggestions, assist features, touch recording, voice input,
handwriting, direct writing, screenshot clipboard, physical-keyboard toolbar and regional
options inside Samsung Keyboard settings. Toggle the feature itself in Samsung's settings.
Revealing a preference does not implement an absent backend.

## Install and use

1. Install the debug APK from the build artifact.
2. Enable HoneyBoard Tweaks in an LSPosed framework implementing **libxposed API 102**
   and remote preferences. Scope: **Samsung Keyboard only**.
3. Open HoneyBoard Tweaks and confirm “Framework connected”.
4. Choose toolbar actions, clipboard capacity and hidden options.
5. Restart Samsung Keyboard or reboot. Toolbar changes require a keyboard process restart.

The only supported base APK is Honeyboard **5.9.40.75**:
`6a011ff31270a53dcc53b891009956a13f4de1ba3d271a6ca4316a6215fae840`.
Other APKs are left untouched by the hooks. No Samsung APK patching or database migration
is performed. The module requests no permissions and has no network access.

Undo/Redo use native context-menu commands. Some editors do not support them; a short
message is shown if the editor rejects the command. The module does not capture text or
implement an artificial undo stack. Logs contain feature stages and exception types only.

## Build and verification

JDK 17, Android SDK 37 (required by libxposed 102):

```sh
./gradlew testDebugUnitTest lintDebug assembleDebug
```

GitHub Actions performs these checks and uploads the APK. Device validation is still pending:
successful unit tests/build do not demonstrate hook execution or visual compatibility.
See [DZIF mapping](docs/DZIF-hooks.md) and [device validation](docs/device-validation.md).

Architecture reference: [OneMate](https://github.com/kalicyh/OneMate), inspected at
`ff02398`. Application and hook implementation are written independently for this project;
OneMate source was not copied. Gradle wrapper files are standard Gradle infrastructure.
