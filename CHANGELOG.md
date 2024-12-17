# Lets-Plot Skia Frontend Changelog

All notable changes to this project will be documented in this file.

The format is based on [Keep a Changelog](https://keepachangelog.com/en/1.0.0/), and this project adheres
to [Semantic Versioning](https://semver.org/spec/v2.0.0.html). All scales should have the 'format' parameter.

## [2.1.1] - 2024-12-17

### Compatibility

- [Android](https://developer.android.com/compose) **temporarily not supported due to [SKIKO-761](https://youtrack.jetbrains.com/issue/SKIKO-761).**
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) 1.7.0 and 1.7.1
- [Skiko](https://github.com/JetBrains/skiko) 0.8.15 and 0.8.18
- [Lets-Plot Kotlin API](https://github.com/JetBrains/lets-plot-kotlin) 4.9.3
- [Lets-Plot Multiplatform](https://github.com/JetBrains/lets-plot) 4.5.2

### Changed

- Kotlin version to 2.1.0
- Lets-Plot Kotlin version to 4.9.3
- Lets-Plot version to 4.5.2


## [2.1.0] - 2024-12-12

### Compatibility

- [Android](https://developer.android.com/compose) **temporarily not supported due to [SKIKO-761](https://youtrack.jetbrains.com/issue/SKIKO-761).**
- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) 1.7.0 and 1.7.1
- [Skiko](https://github.com/JetBrains/skiko) 0.8.15 and 0.8.18
- [Lets-Plot Kotlin API](https://github.com/JetBrains/lets-plot-kotlin) 4.9.2 (and up)
- [Lets-Plot Multiplatform](https://github.com/JetBrains/lets-plot) 4.5.1 (and up)


### Added
- Interactive **links** in tooltips/labels/texts [[LP-1091](https://github.com/JetBrains/lets-plot/issues/1091)].

  See [example notebook](https://nbviewer.org/github/JetBrains/lets-plot-kotlin/blob/master/docs/examples/jupyter-notebooks/f-4.9.0/lp_verse.ipynb).


### Changed

- Kotlin 2.0.20 and Compose multiplatform 1.7.0 support [[#24](https://github.com/JetBrains/lets-plot-skia/issues/24)].


## [2.0.0] - 2024-08-30

### Dependencies

- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) 1.6.10
- [Skiko](https://github.com/JetBrains/skiko) 0.8.4
- [Lets-Plot Kotlin API](https://github.com/JetBrains/lets-plot-kotlin) 4.8.0 (and up)
- [Lets-Plot Multiplatform](https://github.com/JetBrains/lets-plot) 4.4.1 (and up)

> [!IMPORTANT]
> To migrate to this version, you need to update your project build script. 
> 
> See examples in the [lets-plot-compose-demos](https://github.com/JetBrains/lets-plot-compose-demos) repository:
> - [Android minimal](https://github.com/JetBrains/lets-plot-compose-demos/blob/main/compose-android-min/build.gradle.kts) demo.
> - [Android median](https://github.com/JetBrains/lets-plot-compose-demos/blob/main/compose-android-median/build.gradle.kts) demo.
> - [Android animation](https://github.com/JetBrains/lets-plot-compose-demos/blob/main/compose-android-redraw/build.gradle.kts) demo.


### Changed
- Kotlin 2.0.0 and Compose 1.6.10 support [[#11](https://github.com/JetBrains/lets-plot-skia/issues/11)].

## [1.0.4] - 2024-08-26

### Dependencies

- [Compose Multiplatform](https://github.com/JetBrains/compose-multiplatform) 1.6.2
- [Skiko](https://github.com/JetBrains/skiko) 0.7.92
- [Lets-Plot Kotlin API](https://github.com/JetBrains/lets-plot-kotlin) 4.8.0 (and up)
- [Lets-Plot Multiplatform](https://github.com/JetBrains/lets-plot) 4.4.1 (and up)

> Note: 
>  This build is NOT compatible with Lets-Plot v4.3.3 and earlier.

### Fixed
- Sluggish UI on Ubuntu 24.04 [[#13](https://github.com/JetBrains/lets-plot-skia/issues/13)].
- When setting the title to Chinese, Chinese garbled characters appear [[#14](https://github.com/JetBrains/lets-plot-skia/issues/14)].
- fontfamily aes is not supported [[#15](https://github.com/JetBrains/lets-plot-skia/issues/15)].
- theme(exponent="pow") doesn't align text properly [[#19](https://github.com/JetBrains/lets-plot-skia/issues/19)].


## [1.0.3] - 2024-03-21

### Added

- Support for round `clip-path` for `coordPolar()`.
- Support for `geomCurve()`.

### Changed

Dev settings were updated:
- Gradle: v 8.6
- Kotlin: v1.9.22
- Android Gradle Plugin (AGP): v8.2.2 (see notes below)
- Compose Multiplatform: v1.6.1
- Androidx activity-compose: v1.8.2
- Skiko: v0.7.92 (see notes below)

- Lets-Plot Multiplatform: v4.3.0
- Lets-Plot Kotlin API: v4.7.0

> Notes:
>  - Minimum required JDK: 17.
>  - KMP is not yet compatible with AGP 8.3 and up.
>  - Skiko found to have issues with Android devtools (build, emulator):
>    - Skiko v0.7.93 and higher crashes in emulator on ARM arch.
>    - Skiko v0.7.98.1 crashes in emulator on x86 and AMR arch.


## [1.0.2] - 2023-11-30

### Fixed

- Panel flickering when updating data [[#6](https://github.com/JetBrains/lets-plot-skia/issues/6)].


## [1.0.1] - 2023-11-09

### Fixed

- Crashes in Android when rebuild a PlotPanel ("keep aspect ratio" or plot spec change).
- Unexpected redraw [[#2](https://github.com/JetBrains/lets-plot-skia/issues/2)].
- DisposableEffect is not called.


## [1.0.0] - 2023-10-05

### Added

- Support for Android, Compose Desktop and Java Swing platforms.
- Examples in a separate GitHub repository: [lets-plot-compose-demos](https://github.com/JetBrains/lets-plot-compose-demos).
