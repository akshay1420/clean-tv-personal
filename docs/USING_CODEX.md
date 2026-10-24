# Optional: develop or install with Codex

For ordinary installation, use the [direct APK guide](INSTALL.md). No AI tool is required. The instructions below are an advanced alternative for source builds and assisted setup.

This guide sets up this repository's launcher, remote customization, wallpapers, screensaver and optional weather/AQI on **your own** Android TV or Google TV.

**The source runs through a build on your computer. Android runs the resulting APK on the TV. Copying Java files or the GitHub ZIP to a TV does not install an Android app.** A source checkout is not a firmware image or a backup of another person's setup.

## 1. Download the project

Choose **Code > Download ZIP** on [the repository](https://github.com/akshay1420/clean-tv-personal), extract it, and open the extracted folder in your local Codex app. Or clone it:

```sh
git clone https://github.com/akshay1420/clean-tv-personal.git
cd clean-tv-personal
```

Use Codex on the computer on the same network as the TV, with local terminal access. A cloud-only task usually cannot reach a private TV address on your home Wi-Fi. Follow the [official Codex quickstart](https://developers.openai.com/codex/quickstart) to sign in and open a local project. You do not need to upload TV backups to GitHub.

## 2. Prepare your TV

Keep the TV on and the remote nearby. In Settings > About, enable Developer options by pressing the Build entry repeatedly (often seven times). Enable USB debugging; enable Network/ADB debugging if the TV provides it. On supported newer TVs, use Wireless debugging and its displayed pairing/connection ports. Menu names vary by vendor. Find the TV address in Network settings.

Install Android SDK Platform Tools, JDK 17, Android SDK platform 34 and build-tools 34.0.0 on the computer, or let Codex locate/install the missing official tools. Read [Android's ADB guide](https://developer.android.com/tools/adb) for connection details. Do not assume every TV listens on port 5555. Accept the computer's authorization prompt on the physical TV.

## 3. Give Codex this prompt

Open [PROMPT.md](../PROMPT.md), copy it into Codex and fill in the blanks. It tells Codex to back up first, compile the project, install the APKs without clearing data, import the offline photo pack, set up your choices and check the real remote after restart. Give it your own TV address and preferences locally, not in a public issue.

Codex can execute the computer-side commands, but you still need to approve TV connection prompts, choose the default Home, and test physical buttons. Read its tool/permission requests; do not disable the agent's safeguards to make a command work.

## What the setup should do

1. Confirm the connected device, Android version and existing Home. Save package/settings state and APKs of an existing Clean/weather installation **before** updates. Save local photos/configuration where readable. Private app data, accounts, DRM and firmware are not covered by that backup.
2. Build both modules on the computer and verify they use the same local signing key. No private key is supplied by this repository. A certificate mismatch is a reason to stop and plan recovery, not to uninstall the old launcher blindly.
3. Install the launcher APK and, if you want weather/AQI, the companion APK. These installs transfer the APKs to the TV and Android installs them. No manual Java execution on the TV is needed.
4. Open Clean as an ordinary app first. Check navigation, Input, system settings and your apps. Select it as default Home using the TV's Home/app settings if supported. Keep the original Home installed and record how to return to it. If the TV does not support changing Home, Codex should explain the limitation rather than disable system packages automatically.
5. Import the reference photo collection and choose your background. Add your preferred installed apps and button mappings on your own TV. Enable the button helper in Accessibility settings only if you want remapping/redirects.
6. Check Home, volume, Back, Input, playback, the chosen remote buttons, weather and screensaver. Restart once when convenient and check again. Turn network debugging off after successful setup; normal operation does not need ADB.

## Manual build and installation

These commands are an alternative to the Codex prompt. Replace `TV_IP:PORT` with your own connected target. If using paired Wireless debugging, use `adb pair TV_IP:PAIRING_PORT` first, enter the TV's pairing code, then connect to its separately displayed connection port. On a TV with a supported legacy network-debugging toggle, use the port it exposes.

```sh
adb connect TV_IP:PORT
adb devices
```

Only continue when the correct target is listed as `device`, not `unauthorized` or `offline`. Keep `-s TV_IP:PORT` in commands to avoid operating on another connected Android device.

Before installation, record the current Home with:

```sh
adb -s TV_IP:PORT shell cmd package resolve-activity --brief -a android.intent.action.MAIN -c android.intent.category.HOME
```

Have Codex save package/settings state and any existing launcher APKs to an ignored `.local/` folder before continuing. Do not use `adb backup` as a claim of complete recovery; modern Android restricts private app-data backups.

After installation, open Local Weather to allow approximate TV location or search for a city. No source rebuild is needed to change the saved area. For developer builds only, `weather.properties` can optionally supply build-time defaults; keep that file local. A fresh public APK has no selected location and makes no forecast requests until an area is chosen.

Build both modules from the repository root:

```sh
cd launcher
./gradlew --no-daemon testReleaseUnitTest lintRelease assembleRelease
cd ..
```

Windows PowerShell:

```powershell
Set-Location launcher
.\gradlew.bat --no-daemon testReleaseUnitTest lintRelease assembleRelease
Set-Location ..
```

Install the locally built APKs:

```sh
adb -s TV_IP:PORT install -r launcher/app/build/outputs/apk/release/app-release.apk
adb -s TV_IP:PORT install -r launcher/weather/build/outputs/apk/release/weather-release.apk
adb -s TV_IP:PORT shell am start -n com.gathin.tvhome/.MainActivity
```

Do not add `-d`, clear app data, or uninstall a previous launcher to bypass an installation failure. Inspect the failure and signing certificate first. On later updates, keep using the same private signing key so preferences can survive an ordinary `install -r` update.

## Where the options are

| Want to change | Where to go in this generic build |
| --- | --- |
| Wallpaper collection, cropping, fresh wallpaper on Home return | Home > **Launcher settings** > **Backgrounds and screensaver** |
| Clock or photo screensaver, Home idle delay, preview | **Backgrounds and screensaver**; choose Clean in Android screensaver settings for the system saver |
| Branded button's replacement app | **Remote Buttons** > select original app > Change app |
| Learn another supported key | **Remote Buttons** > Add a button > Other button |
| Restore original app behavior | **Remote Buttons** > select original app > Restore original button action |
| Weather/AQI or clock visibility | **Widgets** |
| Android device preferences | **Launcher settings** > TV system settings |

On older themes where the header says Settings and opens Android settings, highlight that on-screen header button and **hold the centre OK button**, then choose **Launcher settings**. Holding blank wallpaper is not a supported shortcut. The remote's physical Settings key can still open Android settings.

The branded app-opening fallback applies to every opening of the selected original app, including manual launches. All mappings start off. Some hardware keys cannot be learned because vendor firmware consumes them before Android's accessibility helper sees them.

## Import the included offline wallpaper pack

The optional [photo pack](../assets/wallpapers/) has 3 nature photos, 7 famous places and 3 night-city views. It contains licensed public reference photographs, not pictures or configuration pulled from a TV. Read [PHOTO-CREDITS.txt](../assets/wallpapers/PHOTO-CREDITS.txt) and preserve attribution. The release APK includes this pack. Load it directly using Backgrounds and screensaver > Load included offline wallpapers. The ADB commands below are an optional advanced alternative; use them before final remote-helper setup.

Run the following from the repository root, with your actual target, on Android versions supporting `content write`:

PowerShell:

```powershell
$tvTarget = 'TV_IP:PORT'
$photoFiles = @(Get-ChildItem assets/wallpapers -Filter '*.jpg') + @(Get-Item assets/wallpapers/captions.txt)
foreach ($photo in $photoFiles) {
    $name = $photo.Name
    adb -s $tvTarget push $photo.FullName "/data/local/tmp/$name"
    if ($LASTEXITCODE -ne 0) { throw "Transfer failed: $name" }
    adb -s $tvTarget shell "content write --uri content://com.gathin.tvhome.theme/wallpapers/$name < /data/local/tmp/$name"
    if ($LASTEXITCODE -ne 0) { throw "Import failed: $name" }
}
```

Bash/macOS/Linux:

```sh
tvTarget='TV_IP:PORT'
for photo in assets/wallpapers/*.jpg assets/wallpapers/captions.txt; do
  name=$(basename "$photo")
  adb -s "$tvTarget" push "$photo" "/data/local/tmp/$name" || break
  adb -s "$tvTarget" shell "content write --uri content://com.gathin.tvhome.theme/wallpapers/$name < /data/local/tmp/$name" || break
done
```

Check the output for unsupported operations or permission failures even if the device reports a zero exit status. Some older Android versions lack this command; Codex should check support and use an appropriate documented theme-file method rather than broadening permissions. After import, open **Backgrounds and screensaver**, choose the collection and return Home. Changing the collection rebuilds Home and reloads the files. Do not force-stop Clean after enabling its accessibility helper; Android may turn that helper off.

The default is dark abstract. For the landmark look choose **Famous places with location details** and **Fill screen**; for the complete image choose **Show whole photo**, which can have borders. Slideshows update every 30 seconds; Home wallpaper changes on return when enabled. Home's idle saver only runs while Home is visible and should not interrupt a movie.

## What will differ from someone else's TV

Installed apps, subscriptions, accounts, remote keys, city, home-picker availability and performance differ. This project does not install streaming services, copy logins, remove streaming ads, or enable a DNS blocker. Install your own apps from sources you trust and choose the mappings yourself. Weather is a coarse modeled feed; AQI is explicitly US AQI, not a local monitoring-station reading. Hardware compatibility needs real-device checks; there is no guarantee that every Google TV supports the same Home handover or branded-key behavior.

## Recovery

Return to the original Home through Android's default-app/Home settings before removing Clean. If a command changed the default, use the recorded original component and have Codex restore it, then verify the Home key. Re-enable debugging if computer-assisted recovery is needed. Keep the signing key, backups and `weather.properties` private and outside public commits. A factory reset removes user-installed apps/preferences and is not needed for ordinary updates.
