# Install directly on Android TV / Google TV

**No Codex, AI tool, source compilation or ADB is required for this method.** Download the Android APK, transfer it to your TV, open it and install it like another Android app. The source ZIP/tar.gz is not executable on a TV.

## Downloads

| File | Purpose |
| --- | --- |
| [Clean-TV-1.0.0.apk](https://github.com/akshay1420/clean-tv-personal/releases/download/v1.0.0/Clean-TV-1.0.0.apk) | Main launcher, remote-button settings, backgrounds and screensaver, including the offline photo collection |
| [Local-Weather-AQI-1.0.0.apk](https://github.com/akshay1420/clean-tv-personal/releases/download/v1.0.0/Local-Weather-AQI-1.0.0.apk) | Optional weather/AQI companion with TV-location permission and city search |
| [Clean-TV-1.0.0-bundle.zip](https://github.com/akshay1420/clean-tv-personal/releases/download/v1.0.0/Clean-TV-1.0.0-bundle.zip) | Convenience bundle containing both APKs and installation notes; extract it first |
| [SHA256SUMS.txt](https://github.com/akshay1420/clean-tv-personal/releases/download/v1.0.0/SHA256SUMS.txt) | Checksums for verifying the downloads |

The **Assets** section on the [release page](https://github.com/akshay1420/clean-tv-personal/releases/tag/v1.0.0) contains these files. Choose an `.apk`, not GitHub's automatically generated **Source code (zip)** or **Source code (tar.gz)** links.

This first direct-install version is a preview: the native APKs build and pass checks, but the new installation/photo/location flows have not been verified on every TV brand. Android 8.0/API 26 or newer is required. Keep the original Home installed while testing.

## 1. Transfer the APK to your TV

Pick whichever route your TV supports:

- **USB drive:** download the APK on a computer/phone, copy it onto a USB drive, connect the drive to the TV, and open the APK using a TV file manager.
- **Local file transfer:** use a file-transfer app you trust on the phone/computer and TV, send the APK to the TV, then open the received file with a file manager.
- **TV browser/download manager:** if your TV has one, open the release page and download the APK directly.

There is no built-in automatic network installer in this repository. You need a way to open the downloaded APK on the TV. If the TV has no file manager, obtain one through its supported app store. No AI assistant is involved in these steps.

## 2. Install

Open `Clean-TV-1.0.0.apk` on the TV. If Android asks, allow **Install unknown apps** for the file manager/browser you are using. Menu names vary: often Settings > Apps > Special app access > Install unknown apps, or Security & restrictions. Return to the APK and choose **Install**, then **Open**. You can turn that source's installation permission off after installing.

The app is named **Clean TV**. If you want weather/AQI, repeat for `Local-Weather-AQI-1.0.0.apk`, named **Local Weather**. Use matching APKs from the same release because the companion's data interface checks their signing certificate.

If Android reports a conflicting package/signature, you may already have an upstream or locally signed build with the same app ID. Do not remove your only working Home or clear its data to force an update. Select the original TV Home, preserve any configuration you need, and use the [advanced guide](USING_CODEX.md) or your existing build's maintainer for migration. This public distribution uses its own signing key; it is not a drop-in update for every development build.

## 3. Make it your Home screen

Try Clean TV as an ordinary app first. Check the remote, Input, TV settings and your streaming apps. Then use Android's default-app/Home selection if your TV provides it: commonly Settings > Apps > Default apps > Home app, or press Home and choose Clean TV > Always when Android offers a chooser.

The path and whether this choice is allowed depend on the manufacturer. Installing a launcher cannot override every vendor's Home restrictions. If no Home selector exists, Clean can still open as an app; this release does not automatically disable Google Home or other system packages. Do not remove the existing Home to create a chooser.

## 4. Set your wallpaper and screensaver

On Clean Home, open the top-right **Launcher settings**, scroll to **Backgrounds and screensaver**, and choose **Load included offline wallpapers**. Wait for the ready message. The 13 public licensed photos are copied locally in a worker thread; your existing files are kept.

Choose **Nature photos**, **Famous places with location details**, or **Night City Lights**. **Fill screen** removes borders by cropping; **Show whole photo** preserves the complete image and can have borders. Enable a fresh wallpaper on each Home return if desired. Dark abstract/plain modes are available without unpacking the photo collection.

Select a screensaver style and use **Preview screensaver**. The Home inactivity timer runs only on Home. For Android's system screensaver, use the TV's screensaver selection and choose Clean TV's **Clock** entry. Availability and naming vary by TV.

The photos make the APK download larger; they do not all stay in RAM. Home still displays one decoded wallpaper at a time. Credits are in **Photo credits and sources** and the repository's wallpaper pack.

## 5. Weather and AQI from TV location

Install/open **Local Weather**, then choose **Use TV location (optional)** and allow approximate-location permission. The app asks the TV's network-location provider for an area, saves it on the TV and requests weather/AQI for it. It rounds a system location to area-level coordinates. There is no continuous background location tracking.

**Permission does not guarantee a location fix.** Some TVs have no location provider or have system Location switched off. If no area is available after the short wait, choose **Choose a city**, enter a city/country using the TV keyboard, search and select the matching result. City search needs no location permission. You can decline location and use city search from the start.

The chosen area and search text are sent to Open-Meteo; no city, coordinates or personal information from another TV are preset in the APK. Weather/AQI is cached separately and refreshed when opened or requested by Home, about every 30 minutes. AQI is explicitly a modeled **US AQI** estimate, not a local station reading or another country's AQI scale.

Return to **Launcher settings > Widgets** to show/hide the Home weather display. You can change the saved area by reopening Local Weather. The two apps must have matching signatures; use the pair from the same release.

## 6. Customize remote buttons

Open **Launcher settings > Remote Buttons**, then **Button helper settings**. Enable Clean TV's helper in Android Accessibility settings if you want this feature. Return and select an original app button, choose **Change app**, and select one of your own installed apps. No mappings are preset.

For vendor-intercepted branded buttons, the optional fallback redirects openings of the original app; this includes manual app launches. Choose **Restore original button action** to turn one off. **Add a button > Other button** learns detectable keys; some remotes/firmware consume their keys before Android can pass them to the helper. Protected power, volume, Input and navigation controls cannot be remapped with this feature.

## 7. Check and update

Test Home, Back, volume, Input, TV settings and playback using the physical remote. Restart once after setup and check Home, mappings and optional weather again. Exact memory usage, Home selection, location and key behavior vary by device.

For updates, download the next APKs from this same project and install over the existing ones using the package installer; matching signatures allow Android to preserve app preferences. The project does not automatically update or collect diagnostics.

## Return to the original TV interface

Choose the original Home through Android's default-app/Home settings before removing Clean TV. If that selector is unavailable, use the manufacturer's supported recovery route or the [advanced guide](USING_CODEX.md) rather than uninstall your only accessible Home.

This project does not install streaming apps, copy accounts, enable a DNS blocker or remove ads from video streams. Those are separate from installing the launcher.
