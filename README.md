# Clean TV Personal

A lightweight home launcher for Android TV and Google TV, based on [CleanYourTV](https://github.com/Gathin23/cleanyourtv). This repository contains reusable source code, not a backup or configuration dump from a TV.

## Features

- Home layout and app ordering, accessible through **Launcher settings** in the top bar.
- **Remote Buttons**: learn detectable keys, or optionally redirect openings of Netflix, YouTube, Prime Video and Google Play to an app chosen on that device. No button mappings are enabled by default. Some vendor firmware intercepts branded keys; redirecting an original app also affects its manual launches. Essential navigation, power, input and volume controls are protected.
- **Backgrounds and screensaver**: changing dark abstract backgrounds; optional nature, landmark and night-city photo collections with captions; moving clock or photo screensavers. Home photo sizing supports full-screen crop or whole-photo fit. Default is dark abstract. No personal photos are bundled.
- **Widgets**: clock/date visibility, optional cached weather and modeled US AQI through a separate small companion. Home has no network permission. Weather requests use a locally configured approximate area, with no GPS, tracking or background polling service.
- No sponsored recommendation feed in the launcher. This does not remove advertising from streaming apps.

## Compatibility

Android 8.0 / API 26 or newer, including Android TV and Google TV devices that allow another Home launcher. Launchers, accessibility settings, remote keys and screensaver selection vary by manufacturer. This is not a Google product, and support on every Google TV model has not been verified. Changing Home is a user choice; this project does not disable system packages or require root. Keep an accessible route to TV system settings.

## Build

Use JDK 17 and Android SDK platform 34 / build-tools 34.0.0. Set `ANDROID_HOME`, or create the ignored `launcher/local.properties` with your SDK directory.

```sh
cd launcher
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease
```

On Windows use `gradlew.bat`. Gradle downloads its pinned distribution and Android dependencies on the first build. APKs are generated under `app/build/outputs/apk/release/` and `weather/build/outputs/apk/release/`; they are ignored by Git. Both modules use the same local Android debug signing key for development. The companion provider enforces a signature permission, so the two apps must be signed with the same key. No signing keys are included. For distribution, configure your own signing securely outside source control.

The existing Android app ID is retained for source compatibility. Installing over an already installed build requires a matching signing certificate; do not uninstall an existing launcher without first preserving access to the original Home and its settings.

## Optional weather and AQI

Copy `launcher/weather.properties.example` to **`launcher/weather.properties`**, set your chosen area name and numeric latitude/longitude, then build the `weather` module. The example coordinates are placeholders, not a saved location. The real configuration is ignored by Git. Without it the companion shows Weather not configured and makes no weather requests. The selected area is compiled into your local companion APK, so do not distribute that APK if you want to keep the area private.

Install both locally built APKs, open **Launcher settings > Widgets**, and enable/view weather. Refreshes are requested about every 30 minutes while Home is visible; the companion caches weather and AQI independently and labels stale readings. This source version uses build-time area configuration; it does not yet have an on-TV city search.

Sources: [Open-Meteo weather](https://open-meteo.com/en/docs), [Open-Meteo air quality](https://open-meteo.com/en/docs/air-quality-api), CAMS ENSEMBLE. AQI is explicitly **modeled US AQI**, not a local monitoring-station reading or another country's AQI scale. Provider attribution is displayed in the companion.

## Optional offline photos

Photo files are not embedded in the launcher APK. To use a collection, import your licensed photos using the upstream [theme file interface](https://github.com/Gathin23/cleanyourtv/blob/main/themes/README.md) / `ThemeProvider` contract in the source. Collections use `nature-`, `world-` or `nightlife-` filename prefixes in the theme's `wallpapers` folder. `captions.txt` uses one `filename<TAB>caption` entry per line. The existing in-app photo credits preserve attribution for the optional reference collection; the actual photo files are not included in this repository. Choose the desired collection under Backgrounds and screensaver. Without imported files, use abstract/plain or an existing theme.

## Privacy and defaults

No TV address, device identifier, serial, Wi-Fi configuration, logs, screenshots, app inventory, account data, saved mappings, weather location, ADB keys, private signing keys or local SDK paths are included. Preferences and mappings start fresh on each installation. Application names used in generic redirect choices describe supported integrations, not the contents of any device.

## Development checks

Run the Gradle unit tests and lint before changing behavior. Validate launcher, remote and screensaver interactions on your own hardware; passing JVM tests is not proof of compatibility with every TV. This publication tree is intentionally separate from the source used for an existing TV installation.

## Attribution and license

MIT, with the original license retained in [LICENSE](LICENSE). Launcher upstream: Gathin23/cleanyourtv, source baseline `eb0131a32fd8567086f39002f1782f7c1b078ca6`. Custom remote, appearance and weather integration is maintained in this repository. Font licensing: [FONT-LICENSE-OFL.txt](launcher/FONT-LICENSE-OFL.txt). Reference photo licensing: `launcher/app/src/main/assets/photo-credits.txt`.
