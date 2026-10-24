# Optional: copy this prompt into local Codex

No AI tool is required for ordinary installation: use [docs/INSTALL.md](docs/INSTALL.md) and the APK downloads instead.

Help me install this repository on my own Android TV / Google TV. Read README.md, docs/USING_CODEX.md and AGENTS.md first. Work locally on the computer on the same network as the TV.

TV brand/model: [fill in]
Android version, if known: [fill in]
TV local address and debugging connection port: [fill in locally]
Wireless pairing required: [yes / no / unsure]
Apps I want on Home, already installed: [fill in]
Remote buttons and replacement apps: [fill in, or leave mappings off]
Background: [dark abstract / nature / famous places / night cities / plain]
Photo sizing: [fill screen with cropping / show whole photo with possible borders]
Screensaver: [dark clock / nature clock / nature / famous places / night cities]
Weather/AQI: [off / on; let me allow TV location or choose a city on the TV]

Do the setup, not just a plan:
1. Locate Git, JDK 17, Android SDK platform 34/build-tools 34.0.0 and ADB. Use official sources for missing tools. Connect to only the TV I identify. Ask me to authorize the computer on the TV or supply a pairing code when needed. Do not assume that Google TV uses port 5555.
2. Before any TV modification, record model/Android/current Home, package enabled states, relevant system settings, installed versions and signing information for these apps. Back up existing Clean/weather APKs, readable theme files and settings into ignored .local/ storage. Explain the private-app-data/DRM/firmware backup limitations. Do not post TV data to GitHub.
3. Check whether Clean is already installed. Preserve its data and signing compatibility. If the certificate differs, stop before any uninstall or data clearing and explain recovery options. Never disable the original Home before confirming an accessible replacement and recording how to restore it.
4. Use on-TV location permission or city selection for weather; configure build-time weather.properties only if specifically requested. Never commit my city/coordinates, address, logs, screenshots, app inventory, ADB keys or signing keys. Build and test both modules. Keep their signing certificates the same. Preserve a private copy of the signing key for later updates.
5. Install the APKs with adb install -r. Launch Clean as an app first. Import assets/wallpapers and captions through the documented protected file interface if I chose photos. Check command output, not just exit codes. Do not overwrite an existing theme without first backing it up.
6. Help me select Clean as default Home using supported TV settings. If that is blocked by firmware, explain the limitation and ask before changing any system package. Do not root, flash firmware, bulk-debloat, change DNS/VPN, disable updates/casting/input/power services, or install extra streaming apps as part of this request.
7. Show me exactly where Launcher settings, Backgrounds and screensaver, Remote Buttons and Widgets are. Apply only the mappings I chose. Explain that branded app redirects affect manual openings too. Let me enable the accessibility helper on the TV; preserve other accessibility services. Use an Android screensaver setting only if I want it.
8. Verify Home, Back, volume, Input, settings access, app playback, chosen remote mappings, wallpapers and optional weather/AQI. Ask me to test physical keys; do not treat injected ADB key events as proof that a physical branded button works. Check relevant crashes and memory use. Restart once at a convenient point and verify persistence. Disable network debugging after successful checks. If anything fails, restore the last working configuration rather than make speculative system changes.
9. Finish with concise results, limitations, backup paths, recovery steps and actual test evidence. Keep my device-specific information local. Do not promise streaming-ad removal or universal Google TV compatibility.
