# Working on this repository

This is generic Android TV / Google TV source. Never commit device addresses, identifying dumps, accounts, private screenshots, installed-app inventories, local weather areas, ADB credentials, signing keys or compiled personal APKs. Use ignored `.local/` for private setup records and backups. Use only placeholders in docs. Source-side tests do not prove a particular TV works.

For an installation request, read `docs/USING_CODEX.md` and `PROMPT.md`. Back up before device writes, select the explicit ADB target, verify the current Home/recovery route, and check signing compatibility before updates. Preserve existing app data and accessibility services. Do not disable system apps, root, change firmware, clear data or uninstall an existing Home without specific user authorization. Normal installation needs neither bulk debloat nor DNS changes. Do not copy a private configuration from another TV.

For code changes, use native Android UI, retain the launcher's lack of network permission, and keep network work in the optional signature-protected companion. No new runtime dependency is needed for installation/docs. Run `testReleaseUnitTest lintRelease assembleRelease` in `launcher/` for Android code changes. For docs/photo changes, check local links, image provenance, placeholder commands and staged files; do not claim hardware tests you did not run.

Always retain upstream MIT attribution, font license and individual photo licenses. Documentation screenshots must be generic or explicitly credited upstream references. Never present a mockup or upstream screenshot as a real-device test of this fork.
