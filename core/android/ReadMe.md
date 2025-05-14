# **Core - Android**

Kotlin implementation of Cross Core SDK for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/io.crosstoken/android-core)

## Requirements

* Android min SDK 23
* Java 11

## Documentation and usage

&nbsp;

## Installation

root/build.gradle.kts:

```gradle
allprojects {
 repositories {
    mavenCentral()
    maven { url "https://jitpack.io" }
 }
}
```

app/build.gradle.kts

```gradle
implementation("io.crosstoken:android-core:release_version")
```
