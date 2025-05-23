# **Cross WC Sign - Kotlin**

Kotlin implementation of Cross WC Sign protocol for Android applications.

![Maven Central](https://img.shields.io/maven-central/v/io.crosstoken/sign)

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
implementation(platform("io.crosstoken:android-bom:{BOM version}"))
implementation("io.crosstoken:android-core")
implementation("io.crosstoken:sign")
```

&nbsp;

## Sample apps

* For sample Dapp run `dapp module`
