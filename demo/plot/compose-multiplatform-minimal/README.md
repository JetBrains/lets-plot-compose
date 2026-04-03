# Compose Multiplatform Minimal Demo

This module is a small Compose Multiplatform Lets-Plot demo with shared UI code and platform-specific entry points.

What it contains:
- shared Compose UI in `src/commonMain`
- a desktop entry point in `src/desktopMain`
- a wasm/js browser entry point in `src/wasmJsMain`
- Android project setup in `build.gradle.kts` and `src/androidMain/AndroidManifest.xml`


Run desktop:

```bash
./gradlew :demo:plot:compose-multiplatform-minimal:run
```

Run wasm/js dev bundle:

```bash
./gradlew :demo:plot:compose-multiplatform-minimal:wasmJsBrowserDevelopmentRun
```

Run Android installDebug task and launch the app on the connected device or emulator:

```bash
./gradlew :demo-plot-compose-multiplatform-minimal:installDebug
```
