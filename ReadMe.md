# **Cross WC SDK - Android**

This repository contains Kotlin implementation of Cross WC SDK protocols for Android applications.

####

## BOM Instructions:

To help manage compatible dependencies stay in sync, we've introduced a [BOM](https://docs.gradle.org/current/userguide/platforms.html#sub:bom_import)
to the Kotlin SDK. With this, you only need to
update the BOM version to get the latest SDKs. Just add the BOM as a dependency and then list the SDKs you want to include into your project.

### example build.gradle.kts

```kotlin
dependencies {
    implementation(platform("io.crosstoken:android-bom:{BOM version}"))
    implementation("io.crosstoken:android-core")
    implementation("io.crosstoken:appkit")
}
```

## SDK Chart

| BOM   | [Core SDK](core/android) | [Sign SDK](protocol/sign) | [AppKit](product/appkit) |
|-------|--------------------------|---------------------------|--------------------------|
| 1.0.0 | 1.0.0                    | 1.0.0                     | 1.0.0                    |

## License

Cross WC SDK is released under the Apache 2.0 license. [See LICENSE](/LICENSE) for details.