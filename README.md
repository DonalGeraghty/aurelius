# Aurelius

A small, offline Android home-screen widget that displays a Stoic thought and changes it approximately once per hour.

## What it does

- Native Android app written in Kotlin.
- Home-screen widget with quote + attribution.
- 72 bundled Stoic quotations/adaptations.
- Deterministic quote for each clock-hour: multiple refreshes in the same hour show the same quote.
- No internet permission, API, account, ads, analytics, server, or database.
- Tapping the widget opens the small companion app.
- Companion app previews the current quote and lets you manually refresh installed widgets.

## Scheduling behaviour

The widget asks Android to update it every 3,600,000 ms (one hour) using `AppWidgetProviderInfo.updatePeriodMillis`.

Android may batch or delay widget updates to protect battery life, so this should be understood as **roughly hourly**, not an exact alarm at `HH:00:00`. Because the displayed quote is calculated from the current local date and hour, a delayed refresh still displays the quote assigned to the current hour.

## Requirements

- Android Studio compatible with Android Gradle Plugin 9.2
- JDK 17+
- Android SDK 37
- Minimum Android version: Android 8.0 (API 26)

## Open and run

1. Clone/download this repository.
2. Open the project folder in Android Studio.
3. If Android Studio asks to install SDK 37 or compatible Gradle tooling, allow it.
4. Run the `app` configuration on your Android phone or emulator.
5. Long-press an empty area of the home screen.
6. Choose **Widgets** → **Aurelius** and drag it onto the home screen.

## GitHub Actions builds

### Debug APK

`.github/workflows/build-apk.yml` builds a debug APK on pushes and pull requests to `main`. The resulting `app-debug.apk` is uploaded as the `Aurelius-debug-apk` workflow artifact.

### Galaxy Store beta bundle

`.github/workflows/build-galaxy-beta.yml` manually builds an **unsigned Android App Bundle (`.aab`)** for a new Galaxy Store app. For a new AAB-based Galaxy Store app, Samsung Seller Portal can manage the app signing key.

1. Open **Actions → Build Galaxy Store Beta**.
2. Choose **Run workflow**.
3. For the first upload use version code `1` and version name `1.0`.
4. Download the `Aurelius-galaxy-beta-1.0` artifact.
5. Extract `app-release.aab`.
6. In Samsung Seller Portal choose **Add New App → Android** and upload the AAB in the Binary section.
7. Use the Galaxy Store signing-key option for the new AAB app.
8. Complete the required App Information fields and then choose **Add Beta Test → Closed Beta Test**.
9. Add your own Samsung account as the tester and submit the beta.
10. Open the beta participation URL on your Samsung phone while signed into that Samsung account.

For subsequent Samsung uploads, increase the Android version code (`2`, `3`, `4`, ...).

### Google Play release bundle

`.github/workflows/build-play-release.yml` manually builds a **signed Android App Bundle (`.aab`)** suitable for uploading to Google Play.

The signing key is deliberately **not stored in this repository**. Create an upload keystore once and keep a secure backup of it.

Example:

```bash
keytool -genkeypair \
  -v \
  -keystore aurelius-upload.jks \
  -alias aurelius \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Convert the keystore to a single-line Base64 string:

```bash
base64 -w 0 aurelius-upload.jks > aurelius-upload.base64.txt
```

In GitHub, open **Settings → Secrets and variables → Actions** and create these repository secrets:

- `AURELIUS_KEYSTORE_BASE64` — contents of `aurelius-upload.base64.txt`
- `AURELIUS_KEYSTORE_PASSWORD` — password used for the keystore
- `AURELIUS_KEY_ALIAS` — for the example above: `aurelius`
- `AURELIUS_KEY_PASSWORD` — password for the key alias

Keep `aurelius-upload.jks` and its passwords somewhere safe outside GitHub. Losing the upload key can complicate future app releases.

Once the secrets are configured:

1. Open **Actions → Build Play Release**.
2. Choose **Run workflow**.
3. Enter a `version_code` and `version_name`.
4. For the first release use version code `1` and version name `1.0`.
5. Every later Play upload must use a larger version code (`2`, `3`, `4`, ...).
6. Download the `Aurelius-play-release-v...` artifact.
7. Upload `app-release.aab` to Google Play Console.

## Project layout

```text
app/src/main/
├── java/com/donalgeraghty/stoicwidget/
│   ├── MainActivity.kt
│   ├── Quote.kt
│   ├── QuoteRepository.kt
│   └── StoicWidgetProvider.kt
└── res/
    ├── drawable/
    ├── layout/
    ├── values/
    └── xml/stoic_widget_info.xml
```

## Possible next features

- Quote frequency setting (hourly / 3-hourly / daily).
- Filter by philosopher.
- Light / dark / transparent widget styles.
- Font-size and widget-density controls.
- Favourite quotes.
- Tap action to advance manually.
- Share quote.
- Exact-hour scheduling as an optional advanced mode.

## Quote note

The bundled text uses concise adaptations of ideas from classical Stoic works rather than claiming a specific modern translation. This keeps attribution clear while avoiding dependence on a particular copyrighted translation.
