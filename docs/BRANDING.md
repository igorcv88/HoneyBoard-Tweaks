# HoneyBoard Tweaks icon assets

- `.github/assets/honeyboard-icon.svg`: standalone scalable project icon, intended for GitHub and project previews.
- `.github/assets/honeyboard-banner.svg`: README header, created from the same icon geometry.
- `app/src/main/res/drawable/ic_launcher_background.xml`: full-bleed Android adaptive background.
- `app/src/main/res/drawable/ic_launcher_foreground.xml`: primary adaptive foreground, composed within the Android launcher safe region.
- `app/src/main/res/drawable/ic_launcher_monochrome.xml`: monochrome glyph for themed icons on Android 13+.
- `app/src/main/res/mipmap-anydpi-v26/` and `mipmap-anydpi-v33/`: adaptive launcher XML resources.

The Android manifest points to `@mipmap/ic_launcher` and `@mipmap/ic_launcher_round`. These are text-based vector resources; no external fonts or raster assets are required in the APK. Check the icon in a launcher with standard, circular and themed masks before merging a release. A release already built from an earlier commit does not automatically acquire this artwork.
