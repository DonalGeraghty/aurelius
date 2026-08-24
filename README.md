# Aurelius

Aurelius is a small, offline Android home-screen widget that displays either a bundled Stoic thought or one of your own personal messages and changes it approximately once per hour.

The intended distribution method is **Google Play Internal Testing**, with the app kept private and installed only by the selected tester account. Aurelius does not need to be published publicly to Google Play.

This README is also intended to act as a **study guide and deployment record** for the project, documenting the important setup and release steps used to build and install the app.

## What it does

- Native Android app written in Kotlin.
- Home-screen widget with either a Stoic quote + attribution or a personal message.
- Two global content modes: **Stoic quotes** and **My messages**.
- Add, edit, and delete personal messages in the companion app.
- Personal messages are stored locally on the device.
- 72 bundled Stoic quotations/adaptations.
- Pseudo-random content from the selected mode on each widget update or manual refresh.
- Resizable from a compact approximately 2×2 layout, with 4×2 as the preferred initial size on Android 12 and newer.
- Responsive compact, standard, and large typography as the widget is resized.
- System, dark, light, and Android 12+ wallpaper-derived color themes.
- Solid or transparent background, three font sizes, and optional attribution.
- No internet permission, API, account, ads, analytics, server, or remote database.
- Tapping the widget opens the small companion app.
- Tapping the displayed quote or attribution immediately chooses another pseudo-random quote.
- Companion app previews the selected content, manages personal messages, switches modes, and lets you manually refresh installed widgets.

## Content modes

**Stoic quotes** is the default mode and preserves the original behaviour for existing installations. Quotes are selected from the bundled offline collection and may include attribution.

**My messages** selects from messages created in the companion app. Messages can be added, edited, and deleted, are limited to 500 characters, and stay on the device in versioned `SharedPreferences` storage. The mode cannot be enabled until at least one message exists. Deleting the final message automatically returns Aurelius to Stoic mode so the widget is never left blank.

The selected mode applies to all installed Aurelius widgets. Switching modes or changing the personal-message collection refreshes the widgets immediately. Personal messages are excluded from Android backup; uninstalling Aurelius or clearing its app data removes them.

## Scheduling behaviour

The widget asks Android to update it every 3,600,000 ms (one hour) using `AppWidgetProviderInfo.updatePeriodMillis`.

Android may batch or delay widget updates to protect battery life, so this should be understood as **roughly hourly**, not an exact alarm at `HH:00:00`. Each update selects pseudo-random content from the active mode; a manual refresh does the same.

## Android identity

```text
Application name: Aurelius
Application ID:   com.donalgeraghty.stoicwidget
Minimum SDK:      26 (Android 8.0)
Target SDK:       37
Compile SDK:      37
```

The application ID is the Android package name used by Google Play. It should be treated as stable once Aurelius is registered in Play Console.

## Requirements

- Android Studio compatible with Android Gradle Plugin 9.2
- JDK 17+
- Android SDK 37
- A Google Play Developer personal account
- A private Android upload keystore

## GitHub Actions

### Build Android APK

Workflow:

```text
.github/workflows/build-apk.yml
```

This runs on pushes and pull requests to `main` and produces a development/debug APK artifact named:

```text
Aurelius-debug-apk
```

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

---

# Google Play Internal Testing: complete setup

## 1. Create the Google Play developer account

A **Personal** Google Play Developer account is sufficient for this project because Aurelius is a personal/hobby application rather than an organisation-owned product.

After the developer account is verified, create a new app in Google Play Console.

Suggested values:

```text
App name: Aurelius
App or game: App
Free or paid: Free
Package name / Application ID: com.donalgeraghty.stoicwidget
```

The package name must match the `applicationId` in the Android project.

## 2. Install Java 17 on Pop!_OS

The upload key is created with the Java `keytool` utility.

If running `keytool` gives:

```text
Command 'keytool' not found
```

install Java 17:

```bash
sudo apt update
sudo apt install openjdk-17-jre-headless
```

Verify that `keytool` is now available:

```bash
keytool -help
```

The Aurelius build is also configured around Java/JVM 17, so this keeps the local tooling consistent with the project.

## 3. Generate the Android upload keystore

Run:

```bash
keytool -genkeypair \
  -v \
  -keystore ~/aurelius-upload.jks \
  -alias aurelius \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

This creates:

```text
~/aurelius-upload.jks
```

During creation, `keytool` asks for information including:

- keystore password
- key password, depending on the Java/keytool flow
- name / organisational identity fields

The important values to remember are the passwords and the alias.

For this project the alias is:

```text
aurelius
```

### Why this key exists

Google Play uses **Play App Signing** for the final distributed application.

The local `aurelius-upload.jks` is the **upload key**. GitHub Actions uses it to sign the `.aab` before the bundle is uploaded to Google Play.

The upload key should never be committed to the repository.

## 4. Back up the keystore

Keep at least two secure backups of:

```text
aurelius-upload.jks
```

For example:

- one encrypted cloud backup
- one offline backup

Also securely record:

- keystore password
- alias: `aurelius`
- key password

Do not store the actual passwords or raw keystore in this README or in Git history.

## 5. Convert the keystore to Base64

GitHub Actions secrets are text values, so the binary `.jks` file is converted to a single-line Base64 string.

Run:

```bash
base64 -w 0 ~/aurelius-upload.jks > ~/aurelius-upload.base64.txt
```

Check that the Base64 file exists:

```bash
ls -lh ~/aurelius-upload.base64.txt
```

To display the value for copying into GitHub:

```bash
cat ~/aurelius-upload.base64.txt
```

The output will be one very long line.

The Base64 file is only a text representation of the keystore. **It is still secret material and must be protected just like the `.jks` file.**

## 6. Add GitHub Actions secrets

In GitHub open:

```text
DonalGeraghty/Aurelius
→ Settings
→ Secrets and variables
→ Actions
→ New repository secret
```

Create exactly these four repository secrets:

### `AURELIUS_KEYSTORE_BASE64`

Value:

The complete single-line output from:

```bash
cat ~/aurelius-upload.base64.txt
```

### `AURELIUS_KEYSTORE_PASSWORD`

Value:

The password chosen for `aurelius-upload.jks`.

### `AURELIUS_KEY_ALIAS`

Value:

```text
aurelius
```

### `AURELIUS_KEY_PASSWORD`

Value:

The password for the `aurelius` key entry.

If the same password was used for both the keystore and the key, then `AURELIUS_KEYSTORE_PASSWORD` and `AURELIUS_KEY_PASSWORD` will contain the same value.

### Important security rule

Only the **names** of these secrets belong in source control.

Never commit:

- the `.jks` file
- the Base64 keystore contents
- keystore passwords
- key passwords

`.gitignore` is configured to exclude common keystore and Base64 export files, but this is only an additional safeguard. Secrets still need to be handled carefully.

## 7. What the GitHub workflow does with the secrets

The workflow reads:

```text
AURELIUS_KEYSTORE_BASE64
AURELIUS_KEYSTORE_PASSWORD
AURELIUS_KEY_ALIAS
AURELIUS_KEY_PASSWORD
```

It then:

1. validates that all required secrets exist
2. decodes the Base64 keystore into a temporary `.jks` file on the GitHub runner
3. supplies the signing values to Gradle as environment variables
4. builds the release Android App Bundle with `:app:bundleRelease`
5. uploads the resulting `.aab` as a GitHub Actions artifact

The temporary keystore exists only on the GitHub Actions runner for that build.

## 8. Run the first Google Play build

In GitHub open:

```text
Actions
→ Build Google Play Internal Test
→ Run workflow
```

For the first release use:

```text
version_code: 1
version_name: 1.0
```

The workflow should produce an artifact similar to:

```text
Aurelius-play-internal-v1.0
```

Download the artifact and extract it.

Inside should be:

```text
app-release.aab
```

This `.aab` is the file uploaded to Google Play Console.

## 9. Upload to Google Play Internal Testing

In Google Play Console open the Aurelius app and go to:

```text
Testing
→ Internal testing
→ Create new release
```

Upload:

```text
app-release.aab
```

If Google asks about Play App Signing during initial setup, follow the Play Console flow to enable it for the app.

## 10. Add the tester account

In the Internal testing section, add the Google account that should be allowed to install Aurelius.

For a private one-person setup, the tester list can contain only that account.

Use the same Google account that is signed into the Google Play Store on the target Android phone.

## 11. Publish the internal test

Save and publish the internal test release.

Google Play will provide an **opt-in / tester link**.

Open that link on the Android phone while signed into the tester Google account.

Accept the invitation and install Aurelius through Google Play.

The app remains on the Internal testing track and does not need to be released publicly.

## 12. Add the widget

After Aurelius is installed:

1. long-press an empty area of the Android home screen
2. choose **Widgets**
3. find **Aurelius**
4. drag the widget onto the home screen

The widget should display a pseudo-random quote and can be resized using the launcher's widget resize handles.

---

# Updating Aurelius later

Whenever a new build is uploaded to Google Play, the `version_code` must increase.

Example sequence:

```text
Release 1: version_code 1, version_name 1.0
Release 2: version_code 2, version_name 1.1
Release 3: version_code 3, version_name 1.2
```

A normal update flow is therefore:

```text
Make code changes
        ↓
Commit/push to GitHub
        ↓
Verify the debug CI build
        ↓
Actions → Build Google Play Internal Test
        ↓
Increase version_code
        ↓
Download new app-release.aab
        ↓
Google Play Console → Internal testing
        ↓
Create/upload new release
        ↓
Publish to internal testers
        ↓
Update Aurelius from Google Play
```

The same upload keystore and GitHub secrets are reused for future releases.

# Signing architecture summary

```text
Local machine
    aurelius-upload.jks
            ↓ Base64
GitHub Actions Secret
    AURELIUS_KEYSTORE_BASE64
            ↓ decoded temporarily
GitHub Actions runner
    aurelius-upload.jks
            ↓ signs bundle
    app-release.aab
            ↓ upload
Google Play Console
            ↓ Play App Signing
Google Play Store
            ↓
Internal tester phone
```

This separation means the signing key material is never stored directly in the repository.

## Local Android Studio development

1. Clone/download this repository.
2. Open the project folder in Android Studio.
3. Allow Android Studio to install SDK 37 or compatible Gradle tooling if requested.
4. Run the `app` configuration on an emulator or permitted Android device.
5. Long-press an empty area of the home screen.
6. Choose **Widgets → Aurelius** and drag it onto the home screen.

Android Studio/ADB installation is a development option only. A managed device may prevent developer-installed apps; Google Play Internal Testing is the intended distribution route for that situation.

## Project layout

```text
app/src/main/
├── java/com/donalgeraghty/stoicwidget/
│   ├── ContentMode.kt
│   ├── MainActivity.kt
│   ├── MessageText.kt
│   ├── PersonalMessage.kt
│   ├── PersonalMessageRepository.kt
│   ├── Quote.kt
│   ├── QuoteRepository.kt
│   ├── StoicWidgetProvider.kt
│   ├── WidgetAppearance.kt
│   ├── WidgetContentSelector.kt
│   └── WidgetPreferences.kt
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
- no remote quote or message service

Stoic quotes are bundled in the app and personal messages are stored locally, so both modes continue to work offline.

## Possible next features

- Quote frequency setting (hourly / 3-hourly / daily).
- Filter by philosopher.
- Favourite quotes.
- Tap action to advance manually.
- Share quote.
- Exact-hour scheduling as an optional advanced mode.

## Quote note

The bundled text uses concise adaptations of ideas from classical Stoic works rather than claiming a specific modern translation. This keeps attribution clear while avoiding dependence on a particular copyrighted translation.

## Additional guide

A shorter companion guide is also available at:

**[docs/GOOGLE_PLAY_INTERNAL_TESTING.md](docs/GOOGLE_PLAY_INTERNAL_TESTING.md)**
