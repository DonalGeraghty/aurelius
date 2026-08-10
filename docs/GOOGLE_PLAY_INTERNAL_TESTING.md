# Google Play Internal Testing

This is the intended distribution method for Aurelius.

Aurelius is a personal app. It does not need to be publicly listed in Google Play. The goal is to upload a signed Android App Bundle to the **Internal testing** track and make the tester list contain only the Google account used on the target Android phone.

## Overview

The release flow is:

```text
GitHub Actions
    ↓
Signed app-release.aab
    ↓
Google Play Console
    ↓
Internal testing track
    ↓
Private tester opt-in link
    ↓
Install through Google Play
```

The repository intentionally does not store the private upload keystore or its passwords.

## 1. Create the app in Google Play Console

Create a new Android app called **Aurelius** in Google Play Console.

Use the existing Android application ID:

```text
com.donalgeraghty.stoicwidget
```

Do not create another Play Console app with a different package name unless you also deliberately change `applicationId` in this repository first. Once an app has been created/uploaded under a package name, treat that package name as permanent for that Play app.

The intended release track is **Testing → Internal testing**. There is no need to publish Aurelius to Production for personal use.

## 2. Create the upload keystore once

On Linux, with a JDK installed:

```bash
keytool -genkeypair \
  -v \
  -keystore aurelius-upload.jks \
  -alias aurelius \
  -keyalg RSA \
  -keysize 2048 \
  -validity 10000
```

Choose and securely record the keystore password and key password.

Keep `aurelius-upload.jks` backed up somewhere private. It must never be committed to this repository.

## 3. Convert the keystore to Base64

On Linux:

```bash
base64 -w 0 aurelius-upload.jks > aurelius-upload.base64.txt
```

The generated text file is only an intermediate representation for adding the key to GitHub Secrets. Keep it private and delete it when it is no longer needed.

## 4. Add GitHub Actions secrets

In the Aurelius GitHub repository open:

**Settings → Secrets and variables → Actions → New repository secret**

Create all four secrets:

| Secret | Value |
|---|---|
| `AURELIUS_KEYSTORE_BASE64` | Entire contents of `aurelius-upload.base64.txt` |
| `AURELIUS_KEYSTORE_PASSWORD` | Password for `aurelius-upload.jks` |
| `AURELIUS_KEY_ALIAS` | `aurelius` if the example command above was used |
| `AURELIUS_KEY_PASSWORD` | Password for the key alias |

The GitHub workflow reconstructs the keystore temporarily on the Actions runner, signs the bundle, and then the runner is discarded.

## 5. Build the first Google Play bundle

In GitHub open:

**Actions → Build Google Play Internal Test → Run workflow**

For the first upload use:

```text
version_code: 1
version_name: 1.0
```

The workflow produces an artifact named approximately:

```text
Aurelius-play-internal-v1.0
```

Download and extract the artifact. The file to upload to Google Play is:

```text
app-release.aab
```

## 6. Upload to Internal testing

In Google Play Console:

1. Open Aurelius.
2. Go to **Testing → Internal testing**.
3. Create an internal-test release.
4. Upload `app-release.aab`.
5. Complete any Play Console setup items that are required before the release can be made available.
6. Add the Google account used on the target phone to the internal tester list.
7. Save/publish the internal-test release.
8. Copy the tester opt-in link.

## 7. Install on the phone

On the Android phone:

1. Make sure Google Play is signed in with the Google account that was added as a tester.
2. Open the internal-test opt-in link.
3. Join the test.
4. Open the Google Play listing offered by the test page.
5. Install Aurelius through Google Play.
6. Open Aurelius once.
7. Long-press an empty area of the home screen.
8. Choose **Widgets → Aurelius** and add the widget.

Because installation is handled by Google Play, this avoids manually installing the debug APK. A device-management policy can still block an app if the organisation explicitly disallows it; this workflow does not attempt to bypass device-management policy.

## Updating Aurelius later

Every new AAB uploaded to Google Play must have a larger `version_code` than the previous upload.

Example:

| Release | version_code | version_name |
|---|---:|---|
| First | 1 | 1.0 |
| Second | 2 | 1.1 |
| Third | 3 | 1.2 |

Run **Build Google Play Internal Test** again with the new values, upload the new AAB to the Internal testing track, and Google Play can deliver the update to the tester device.

## Signing terminology

The key stored in GitHub Secrets is the **upload key** used to authenticate uploads. Google Play App Signing can manage the app-signing key used for distributed APKs. Keep the upload keystore backed up even when Play App Signing is enabled.

## Development APK

The repository still has `Build Android APK` for normal development and emulator/unmanaged-device testing. That debug APK is not the intended distribution route for the managed phone and should not be uploaded to Google Play.
