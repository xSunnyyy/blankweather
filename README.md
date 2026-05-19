# Blank Weather

A minimalist Android weather app inspired by the reference design — clean
typography, line icons, light & dark theme, pull-to-refresh, location
permissions, and the free [Open-Meteo](https://open-meteo.com/) API for
forecasts.

## Download the APK

Every push to `claude/android-weather-app-njkFk` (or `main`) runs the
**Build APK** workflow under
[Actions](../../actions/workflows/build-apk.yml). The build is also
published as a rolling GitHub Release tagged **`latest`**, so the easiest
way to grab the APK on your phone is:

1. Open the [latest release](../../releases/tag/latest) in your phone's
   browser.
2. Tap `BlankWeather.apk` to download.
3. Open the downloaded file and tap **Install**. Android will prompt you
   to allow installing from unknown sources for your browser the first
   time — accept it, then return and tap Install again.

Or via `adb` from a computer:

```sh
adb install BlankWeather.apk
```

The APK is debug-signed (so signature won't match an existing Play
install), and minSdk is 26 (Android 8.0+).

## Features

- Big numeric temperature in the style of the mockup
- Six 3-hour forecast slots with weather icon and precipitation probability
- Seven-day daily forecast with min/max
- Pull down to refresh
- Light & dark mode follow the system setting
- Foreground/coarse location with in-app permission prompt

## Build locally

```sh
./gradlew :app:assembleRelease
# APK at app/build/outputs/apk/release/app-release.apk
```

Requires Android SDK with platform 34 installed and JDK 17.
