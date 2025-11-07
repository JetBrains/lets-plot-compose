### Configuring IntelliJ IDEA for Android Development

- #### Android Plugin

Make sure the [Android plugin](https://plugins.jetbrains.com/plugin/22989-android) is installed.

- #### Android SDK

From menu `Tools -> Android -> SDK Manager` setup an Android SDK.

The `local.properties` file will be automatically generated.
In case the `local.properties` file didn't appear in the project root:
- create it manually
- add property `sdk.dir` pointing to the location of the Android SDK on your system.

For example:
```
sdk.dir=/Users/john/Library/Android/sdk
```
- #### Android Device

From menu `Tools -> Android -> Device Manager` setup Android device.

For example, Nexus 10 with Android 12 works well.

- #### Java 11 Toolchain

Make sure the Java 11 toolchain is installed. Otherwise, the following error may occur:
>Cannot find a Java installation on your machine matching this tasks requirements: {languageVersion=11, vendor=any, implementation=vendor-specific}
> 
>No locally installed toolchains match and toolchain download repositories have not been configured.


### Running Android Demos

In IDEA `Run Configurations`
- Select `demo-plot-compose-android-min`, `demo-plot-compose-android-median` or another _Android App Run Configuration_
- Select the `Android` device
- Click `Run`


### See also

[Compose-multiplatform iOS/Android Template](https://github.com/JetBrains/compose-multiplatform-ios-android-template)  
[Compose-multiplatform Versioning](https://github.com/JetBrains/compose-multiplatform/blob/master/VERSIONING.md)

