# Repository Guidelines

## Project Structure & Module Organization

This repository contains one Android application module, `app`, built with Kotlin and Jetpack Compose.

- Production code is under `app/src/main/java/com/example/poke_android/`.
  - `data/` contains Retrofit APIs, serializable models, the repository, and encrypted session storage.
  - `ui/` contains Compose screens, navigation, theme definitions, UI state, and the `PokeViewModel`.
- Unit tests are in `app/src/test/`; device and Compose UI tests are in `app/src/androidTest/`.
- Android resources and the manifest are under `app/src/main/res/` and `app/src/main/`.
- `README.md` documents the product; `ROADMAP.md` tracks planned work; `.github/workflows/android.yml` defines CI.

## Build, Test, and Development Commands

Use the Gradle wrapper with JDK 17+, Android SDK 36, and a configured `ANDROID_HOME` (or `local.properties`).

```sh
./gradlew assembleDebug                 # Build the debug APK
./gradlew testDebugUnitTest             # Run JVM unit tests
./gradlew lintDebug                     # Run Android lint
./gradlew assembleDebug testDebugUnitTest lintDebug  # Match CI verification
./gradlew connectedDebugAndroidTest     # Run instrumented tests on a device/emulator
```

The debug APK is written to `app/build/outputs/apk/debug/app-debug.apk`. Network contract tests use MockWebServer; do not point them at production.

## Coding Style & Naming Conventions

Follow Kotlin conventions with four-space indentation, trailing commas where they improve multiline diffs, and expression-oriented code where readable. Use `PascalCase` for classes and composables, `camelCase` for functions, properties, and parameters, and descriptive `*Test` class names. Keep Compose UI in `ui/`, backend and persistence concerns in `data/`, and state transitions in the ViewModel. No separate formatter or linter configuration is checked in; run `lintDebug` before submitting changes.

## Testing Guidelines

JUnit 4 and `kotlinx-coroutines-test` cover repository and ViewModel behavior. Compose instrumentation tests cover UI interactions, navigation, and encrypted session persistence. Name tests after observable behavior, such as `failedStageSaveLocksAnswerAndReusesUuid`. Add or update tests for API, state, and navigation changes; run both JVM and connected tests when an emulator is available.

## Commit & Pull Request Guidelines

Existing commits use Conventional Commit-style prefixes such as `feat:` and `fix:` followed by an imperative, specific summary. Keep commits focused. Pull requests should explain the user-visible or architectural change, link the relevant issue or roadmap item when applicable, list verification commands, and include screenshots for visual Compose changes. Call out API, configuration, or migration requirements explicitly.

## Security & Configuration Tips

Do not commit `local.properties`, credentials, access tokens, or production data. The API URL is configured through `BuildConfig.API_URL` in `app/build.gradle.kts`; session tokens must remain in the encrypted `SessionStore` and must never be logged.
