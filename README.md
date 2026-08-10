# StoicWidget

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
6. Choose **Widgets** → **StoicWidget** and drag it onto the home screen.

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
