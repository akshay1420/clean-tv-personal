# Clean TV Personal

A lightweight home launcher for Android TV and Google TV, based on [CleanYourTV](https://github.com/Gathin23/cleanyourtv). This repository contains reusable source code, not a backup or configuration dump from a TV.

## Launcher preview

These are **upstream CleanYourTV reference screenshots**, credited to [Gathin23/cleanyourtv](https://github.com/Gathin23/cleanyourtv/tree/eb0131a32fd8567086f39002f1782f7c1b078ca6). They show the shared base launcher and clock, not a test of this fork or a private TV configuration. This fork adds the settings/features listed below; its default is dark abstract and your apps/layout can differ.

| Home and app dock | Clock screensaver |
| --- | --- |
| ![Upstream reference: launcher Home with app dock](docs/images/upstream-home.png) | ![Upstream reference: moving-clock screensaver](docs/images/upstream-screensaver.png) |

Screenshot source and license: [image credits](docs/images/README.md). No screenshots from a private TV setup are published here.

## Download and install — no AI or computer build required

**[Download Clean TV APK](https://github.com/akshay1420/clean-tv-personal/releases/download/v1.0.0/Clean-TV-1.0.0.apk)** · **[Optional weather/AQI APK](https://github.com/akshay1420/clean-tv-personal/releases/download/v1.0.0/Local-Weather-AQI-1.0.0.apk)** · **[All release files](https://github.com/akshay1420/clean-tv-personal/releases/tag/v1.0.0)**

1. Download the **APK**, transfer it to your Android TV / Google TV by USB drive or a file-transfer app, and open it using the TV's file manager.
2. Allow installation for that file-manager source if prompted, then choose **Install > Open**. The launcher appears as **Clean TV**. No Codex, source download, ADB or compilation is needed for this path.
3. Open **Launcher settings** to choose backgrounds, screensaver, widgets and remote buttons. Choose **Load included offline wallpapers** once to unpack the licensed collection on the TV. Select Clean TV as default Home where your TV supports it.
4. For weather/AQI, also install **Local Weather**, open it, and choose **Use TV location** and grant approximate-location permission, or **Choose a city** if your TV cannot provide a location.

Read the [TV installation guide](docs/INSTALL.md) for transfer methods, permissions, Home selection, recovery and troubleshooting. Android installs APK files; it does **not** execute a gzip/tar.gz file. GitHub's automatic **Source code** ZIP/tar.gz downloads are for developers, not TV installation. An optional ZIP bundle on the release page contains both APKs; extract it before installation.

This first direct-install release is a preview. Builds, unit tests and lint are checked; the new city/location and bundled-photo controls still need real-device coverage across different TV brands. No personal TV data is included.

### Optional: source and Codex workflow

Developers can clone/download the source and build it. [The Codex/manual development guide](docs/USING_CODEX.md) and [PROMPT.md](PROMPT.md) remain available for custom builds and assisted ADB setup. They are **not required** to install the downloadable APKs.

## Features

- Home layout and app ordering, accessible through **Launcher settings** in the top bar.
- **Remote Buttons**: learn detectable keys, or optionally redirect openings of Netflix, YouTube, Prime Video and Google Play to an app chosen on that device. No button mappings are enabled by default. Some vendor firmware intercepts branded keys; redirecting an original app also affects its manual launches. Essential navigation, power, input and volume controls are protected.
- **Backgrounds and screensaver**: changing dark abstract backgrounds; optional nature, landmark and night-city photo collections with captions; moving clock or photo screensavers. Home photo sizing supports full-screen crop or whole-photo fit. Default is dark abstract. No personal photos are bundled.
- **Widgets**: clock/date visibility, optional cached weather and modeled US AQI through a separate small companion. Home has no network permission. Users can grant optional approximate TV location or search for a city; there is no background location tracking.
- No sponsored recommendation feed in the launcher. This does not remove advertising from streaming apps.

## Compatibility

Android 8.0 / API 26 or newer, including Android TV and Google TV devices that allow another Home launcher. Launchers, accessibility settings, remote keys and screensaver selection vary by manufacturer. This is not a Google product, and support on every Google TV model has not been verified. Changing Home is a user choice; this project does not disable system packages or require root. Keep an accessible route to TV system settings.

## Build

Use JDK 17 and Android SDK platform 34 / build-tools 34.0.0. Set `ANDROID_HOME`, or create the ignored `launcher/local.properties` with your SDK directory.

```sh
cd launcher
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease
```

On Windows use `gradlew.bat`. Gradle downloads its pinned distribution and Android dependencies on the first build. APKs are generated under `app/build/outputs/apk/release/` and `weather/build/outputs/apk/release/`; they are ignored by Git. Both modules use the same local Android debug signing key for development. Published release APKs instead share a separate private distribution key, supplied through `CLEAN_TV_KEYSTORE` and `CLEAN_TV_STORE_PASSWORD` environment variables when built by the maintainer. The companion provider enforces a signature permission, so the two apps must be signed with the same key. No signing keys are included. For distribution, configure your own signing securely outside source control.

The existing Android app ID is retained for source compatibility. Installing over an already installed build requires a matching signing certificate; do not uninstall an existing launcher without first preserving access to the original Home and its settings.

## Optional weather and AQI

Install the weather/AQI APK from the same release as the launcher. Open **Local Weather** and choose **Use TV location (optional)**. The app asks for approximate-location permission only when you choose that action. If the TV provides a network location, that area is saved on the TV and used for both forecasts and AQI. Permission alone cannot provide a fix on TVs without a working location provider: **Choose a city** is the fallback and needs no location permission.

City search and the chosen area are sent to Open-Meteo to obtain results. No location is preset in the public APKs; there is no GPS polling, background location service, account or personal data from another TV. Weather and AQI cache independently, refresh about every 30 minutes while Home is visible, and retain old observations on a network failure. Change the saved area in Local Weather anytime. Developers can optionally use the ignored `launcher/weather.properties` for their own build-time defaults, but this is not needed for direct installation.

Sources: [Open-Meteo weather](https://open-meteo.com/en/docs), [air quality](https://open-meteo.com/en/docs/air-quality-api), [city search](https://open-meteo.com/en/docs/geocoding-api), CAMS ENSEMBLE and GeoNames. AQI is explicitly **modeled US AQI**, not a local monitoring-station reading or another country's AQI scale. Attribution is displayed in the companion.

## Optional offline photos

The repository includes 13 licensed public reference photographs in [assets/wallpapers/](assets/wallpapers/): 3 nature, 7 famous places and 3 night-city views. They are licensed public assets, not private photos or a TV data export. They are bundled in the direct-install launcher APK. Open **Backgrounds and screensaver > Load included offline wallpapers**, then choose a collection. Loading runs off the UI thread and preserves existing files. The download is larger because it includes photos; normal playback still decodes one wallpaper at a time. Default dark abstract needs no photo loading. Advanced ADB [import instructions](docs/USING_CODEX.md#import-the-included-offline-wallpaper-pack) remain available.

Keep the authors, source links and per-image licenses in [PHOTO-CREDITS.txt](assets/wallpapers/PHOTO-CREDITS.txt) and [manifest.json](assets/wallpapers/manifest.json). Caption format is one `filename<TAB>caption` per line. Imported collections use `nature-`, `world-` or `nightlife-` prefixes in the theme's `wallpapers` folder. The protected `ThemeProvider` contract is in the source; upstream [theme documentation](https://github.com/Gathin23/cleanyourtv/blob/main/themes/README.md) provides further background.

## Privacy and defaults

No private TV address, device identifier, serial, Wi-Fi configuration, logs, screenshots, app inventory, account data, saved mappings, weather location, ADB keys, private signing keys or local SDK paths are included. The preview images are credited public upstream references, and the wallpaper pack contains licensed public photographs. Preferences and mappings start fresh on each installation. Application names used in generic redirect choices describe supported integrations, not the contents of any device.

## Development checks

Run the Gradle unit tests and lint before changing behavior. Validate launcher, remote and screensaver interactions on your own hardware; passing JVM tests is not proof of compatibility with every TV. This publication tree is intentionally separate from the source used for an existing TV installation.

## Attribution and license

Source code is MIT, with the original license retained in [LICENSE](LICENSE). Photos retain their individual licenses; see the wallpaper credits. Screenshot credits are in [docs/images/README.md](docs/images/README.md). Launcher upstream: Gathin23/cleanyourtv, source baseline `eb0131a32fd8567086f39002f1782f7c1b078ca6`. Custom remote, appearance and weather integration is maintained in this repository. Font licensing: [FONT-LICENSE-OFL.txt](launcher/FONT-LICENSE-OFL.txt). Reference photo licensing: `launcher/app/src/main/assets/photo-credits.txt`.
