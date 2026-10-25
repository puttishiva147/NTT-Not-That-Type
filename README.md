# NTT — Not That Type

NTT is an Android screen-understanding and accessibility assistant. It is designed to help users read on-screen text and numbers, understand regional number/price formats, and access quick speech and floating-assistant controls.

> **Important:** NTT uses Android accessibility and overlay capabilities. Enable these only if you understand and want the associated functionality. Behaviour depends on the permissions granted on your device.

## Current app details

- **App name:** NTT
- **Application ID:** `com.aistudio.ntt.qrvxmb`
- **Version:** `2.1.3` (version code `7`)
- **Minimum Android version:** Android 7.0 (API 24)
- **Target SDK:** Android 16 (API 36)
- **Technology:** Kotlin, Jetpack Compose, Android services, Firebase libraries

## What it is intended to do

- **On-screen assistance:** An Android Accessibility Service supports the app's screen-reading and text/number-understanding workflow.
- **Floating assistant:** A foreground service supports an on-screen floating control/assistant.
- **Quick Settings tile:** An NTT tile provides a quick entry point for its microphone/speech-related action.
- **Speech and number help:** The app's current resource strings describe speaking numbers or regional words and reading on-screen text and numbers.

The exact capabilities available in a given build depend on the implementation and permissions enabled on the device.

## Permissions

Depending on which features you use, Android may request or require:

- Internet and network state
- Accessibility Service access
- Display over other apps
- Microphone
- Camera (declared as optional hardware)
- Notifications and foreground-service permissions
- Vibration

Grant only the permissions needed for the features you choose. Accessibility access can expose on-screen content to an accessibility service; do not enable it unless you trust the app and understand its purpose.

## Build the debug APK

The repository includes a GitHub Actions workflow at `.github/workflows/build.yml` intended to build and upload a debug APK.

1. Open the repository's **Actions** tab.
2. Select **Build Android APK**.
3. Choose **Run workflow** on the `main` branch, or push a commit to `main`.
4. Open the completed run and download the `ntt-app-debug` artifact.
5. Extract the downloaded ZIP to get the APK.

The debug APK is for testing and is signed with a debug key; it is not a production release. A release APK needs a properly configured release signing key and secure GitHub Actions secrets.

### Local build

With a compatible JDK, Android SDK, and Gradle wrapper available:

```bash
./gradlew :app:assembleDebug
```

The APK is normally generated at:

```
app/build/outputs/apk/debug/app-debug.apk
```

## Troubleshooting

- If the build fails, open the failed GitHub Actions run and inspect the Gradle error in the logs.
- If a feature does not work, check Android Settings for the specific permission or service it requires.
- Never commit keystores, passwords, API keys, or other secrets to the repository.

## Project status

This README documents the current Android project configuration and declared services. It does not claim that every feature has been independently tested on a physical device.

## License

No license is specified here. Unless a license is added to the repository, standard copyright restrictions apply.
