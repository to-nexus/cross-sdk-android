import java.util.Properties

plugins {
    id(libs.plugins.android.application.get().pluginId)
    id(libs.plugins.kotlin.android.get().pluginId)
    id(libs.plugins.kotlin.kapt.get().pluginId)
    //alias(libs.plugins.google.services)
    //alias(libs.plugins.firebase.crashlytics)
    id("signing-config")
}

android {
    namespace = "io.crosstoken.sample.modal"
    compileSdk = COMPILE_SDK

    defaultConfig {
        applicationId = "io.crosstoken.sample.modal"
        minSdk = MIN_SDK
        targetSdk = TARGET_SDK
        versionName = SAMPLE_VERSION_NAME

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        vectorDrawables {
            useSupportLibrary = true
        }
        buildConfigField("String", "PROJECT_ID", "\"${getSecretProperty("CROSS_PROJECT_ID")}\"")
        buildConfigField("String", "BOM_VERSION", "\"${BOM_VERSION}\"")
    }

    buildTypes {
        val appLink = "https://dev-cross-sdk-js.crosstoken.io"

        getByName("release") {
            manifestPlaceholders["appLink"] = appLink
            buildConfigField("String", "LAB_APP_LINK", "\"$appLink\"")
        }

        getByName("internal") {
            manifestPlaceholders["appLink"] = appLink
            buildConfigField("String", "LAB_APP_LINK", "\"$appLink\"")
        }

        getByName("debug") {
            manifestPlaceholders["appLink"] = appLink
            buildConfigField("String", "LAB_APP_LINK", "\"$appLink\"")
        }
    }

    lint {
        abortOnError = true
        ignoreWarnings = true
        warningsAsErrors = false
    }

    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }
    kotlinOptions {
        jvmTarget = jvmVersion.toString()
        freeCompilerArgs = listOf("-Xcontext-receivers")
    }
    buildFeatures {
        compose = true
        buildConfig = true
        viewBinding = true
    }
    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeCompiler.get()
    }
}

fun getSecretProperty(key: String): String {
    return System.getenv(key) ?: rootProject.file("secrets.properties").let { secretsFile ->
        check(secretsFile.exists()) { "Secrets file not found at path: ${secretsFile.absolutePath}" }
        Properties().apply {
            load(secretsFile.inputStream())
        }
    }.getProperty(key)
}

dependencies {
    implementation(project(":sample:common"))
    implementation("androidx.compose.material:material-icons-core:1.7.1")

//    implementation(platform(libs.firebase.bom))
//    implementation(libs.bundles.firebase)

    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material)
    implementation(libs.androidx.compose.navigation)
    implementation(libs.androidx.compose.lifecycle)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    androidTestImplementation(libs.androidx.compose.ui.test.junit)
    androidTestImplementation(libs.androidx.compose.navigation.testing)

    implementation(libs.bundles.accompanist)
    implementation(libs.bundles.androidxAppCompat)
    implementation(libs.bundles.androidxLifecycle)
    api(libs.bundles.androidxNavigation)

    debugImplementation(project(":core:android"))
    debugImplementation(project(":product:appkit"))

    internalImplementation(project(":core:android"))
    internalImplementation(project(":product:appkit"))

    releaseImplementation(platform("io.crosstoken:android-bom:$BOM_VERSION"))
    releaseImplementation("io.crosstoken:android-core")
    releaseImplementation("io.crosstoken:appkit")
}