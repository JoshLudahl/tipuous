# Tipuous Project Guidelines for Junie

Last updated: 2025-08-15 18:49

## Overview
Tipuous is an Android application written in Kotlin using Jetpack Compose for UI, Room for local persistence, and standard Android/Gradle tooling. The project follows a single-module structure (app module) and includes unit tests and instrumentation tests.

Key domains:
- UI: Jetpack Compose screens (Main, Receipts, Settings)
- Data: Room (AppDatabase, ReceiptDao, ReceiptEntity), repository pattern (ReceiptRepository)
- Utilities: OCR helper, conversions

## Project Structure (high level)
- app/src/main/java/com/tips/tipuous
  - data/ … repository and Room entities/DAO
  - model/ … simple data models
  - navigation/ … app navigation
  - ui/ … Compose UI (screens, view models, theme, components)
  - utilities/ … helper utilities (e.g., ReceiptOcr)
- app/src/main/res … Android resources (drawables, fonts, values)
- app/src/test … JVM unit tests
- app/src/androidTest … instrumentation/UI tests
- build.gradle.kts, settings.gradle.kts, gradle/ … Gradle build configuration
- ktlint.gradle … Kotlin style checks
- pre-commit.sh … convenience script for local checks

## Build and Run
- Minimum required tools: Android Studio (Giraffe+ recommended) with Android SDK, Java toolchain managed by Gradle. CLI builds use Gradle Wrapper.
- Build the project:
  - CLI: ./gradlew assemble or ./gradlew build
  - From Android Studio: standard Build/Run actions
- Signing: A release keystore is present (tipuous_keystyore.jks). Debug builds use default debug keystore.
- Google services: app/google-services.json is included; ensure Google Services Gradle plugin is available via Gradle.

## Testing
- Unit tests (JVM): located under app/src/test
  - CLI: ./gradlew :app:testDebugUnitTest
  - All variants: ./gradlew test
- Instrumentation tests (device/emulator required): app/src/androidTest
  - CLI: ./gradlew :app:connectedDebugAndroidTest
  - Ensure an emulator or device is connected and authorized.

Notes for Junie automation:
- Prefer running unit tests for fast feedback. Only run instrumentation tests when necessary due to device requirements.
- If using this environment’s testing tool, target:
  - Unit tests path: app/src/test/java/com/tips/tipuous
  - Instrumentation tests path: app/src/androidTest/java/com/tips/tipuous

## Code Style & Quality
- Kotlin style checks via ktlint.gradle
  - Check: ./gradlew ktlintCheck
  - Fix (if configured): ./gradlew ktlintFormat
- Keep functions small and focused. Favor immutable data where possible.
- Follow Jetpack Compose best practices (state hoisting, previews for composables when practical).
- Use MVVM architecture
- Use DRY principles

## CI/CD (general guidance)
- Ensure build, unit tests, and ktlintCheck pass on every change.
- For pull requests: run
  - ./gradlew clean build ktlintCheck
  - Optionally run instrumentation tests on a CI runner that supports Android emulators.

## Common Tasks
- Add a new Room entity:
  - Create @Entity data class and @Dao
  - Add to AppDatabase and handle migrations if schema changes require it
- Add a new screen:
  - Create composable under ui/.../YourScreen.kt and a ViewModel if needed
  - Wire into navigation in navigation/Navigation.kt

## Troubleshooting
- Gradle sync issues: ensure Gradle wrapper and Android Gradle Plugin versions match in gradle/libs.versions.toml and build scripts.
- Google Services: if build plugin complains, verify the classpath and google-services.json placement under app/.
- OCR utilities may require runtime permissions (camera, storage) depending on usage; handle in UI as needed.

## Expectations for Junie
- Make minimal, targeted changes to satisfy issues.
- When modifying code, run unit tests and ktlintCheck locally.
- Provide clear updates and a brief summary of what changed and why.
