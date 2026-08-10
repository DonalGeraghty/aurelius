# Aurelius

Aurelius is a small, offline Android home-screen widget that displays a Stoic thought and changes it approximately once per hour.

The intended distribution method is **Google Play Internal Testing**, with the app kept private and installed only by the selected tester account. Aurelius does not need to be published publicly to Google Play.

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

## Android identity

```text
Application name: Aurelius
Application ID:   com.donalgeraghty.stoicwidget
Minimum SDK:      26 (Android 8.0)
Target SDK:       37
Compile SDK:      37
```

The application ID should be treated as stable once Aurelius is registered/uploaded in Google Play Console.

## Requirements

- Android Studio compatible with Android Gradle Plugin 9.2
- JDK 17+
- Android SDK 37

## GitHub Actions

### Build Android APK

Workflow:

```text
.github/workflows/build-apk.yml
```

This runs on pushes and pull requests to `main` and produces a development/debug APK artifact named `Aurelius-debug-apk`.

The debug APK is useful for emulators and unmanaged Android devices. It is **not** the intended installation method for a managed work phone and should not be uploaded to Google Play.

### Build Google Play Internal Test

Workflow:

```text
.github/workflows/build-play-release.yml
```

This is a manually triggered workflow that builds a **signed Android App Bundle (`.aab`)** for Google Play Internal Testing.

It asks for:

- `version_code` — positive integer that must increase for each Play upload.
- `version_name` — user-visible version such as `1.0` or `1.1`.

The resulting artifact is named approximately:

```text
Aurelius-play-internal-v1.0
```

and contains:

```text
app-release.aab
```

## Google Play setup

Aurelius is designed to stay on the **Internal testing** track. The tester list can contain only the Google account that should be allowed to install the app.

The complete setup guide is here:

**[Google Play Internal Testing guide](docs/GOOGLE_PLAY_INTERNAL_TESTING.md)**

The high-level flow is:

```text
Create Aurelius in Play Console
        ↓
Create private upload keystore
        ↓
Store keystore/passwords in GitHub Actions Secrets
        ↓
Run "Build Google Play Internal Test"
        ↓
Download signed app-release.aab
        ↓
Upload to Play Console → Testing → Internal testing
        ↓
Add tester Google account
        ↓
Open tester opt-in link on phone
        ↓
Install Aurelius through Google Play
```

## Required GitHub Secrets

The Play build requires four repository secrets:

- `AURELIUS_KEYSTORE_BASE64`
- `AURELIUS_KEYSTORE_PASSWORD`
- `AURELIUS_KEY_ALIAS`
- `AURELIUS_KEY_PASSWORD`

The private keystore itself is deliberately not stored in the repository. `.gitignore` excludes common Android keystore files and the temporary Base64 export file.

See the [Google Play Internal Testing guide](docs/GOOGLE_PLAY_INTERNAL_TESTING.md) for the exact `keytool` and Base64 commands.

## Versioning

For the first internal-test build use:

```text
version_code: 1
version_name: 1.0
```

For every later Google Play upload, increase `version_code`:

```text
1 → 2 → 3 → 4 ...
```

`version_name` can follow a normal user-facing sequence such as `1.0`, `1.1`, `1.2`, and so on.

## Open locally in Android Studio

1. Clone/download this repository.
2. Open the project folder in Android Studio.
3. Allow Android Studio to install SDK 37 or compatible Gradle tooling if requested.
4. Run the `app` configuration on an emulator or permitted Android device.
5. Long-press an empty area of the home screen.
6. Choose **Widgets → Aurelius** and drag it onto the home screen.

Android Studio/ADB installation is a development option only. A managed device may prevent developer-installed apps; Google Play Internal Testing is the intended route for the managed phone.

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

## Privacy

Aurelius is intentionally self-contained:

- no network permission
- no account/login
- no backend
- no advertising
- no analytics
- no remote quote service

Quotes are bundled in the app, so the widget continues to work offline.

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
