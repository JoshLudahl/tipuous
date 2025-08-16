plugins {
    id("com.android.application")
    id("kotlin-android")
    id("com.google.gms.google-services")
    id("com.google.firebase.crashlytics")
    id("com.google.firebase.firebase-perf")
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serializable)
}

android {
    val targetVersion = 36

    compileSdk = targetVersion

    defaultConfig {
        applicationId = "com.tips.tipuous"
        minSdk = 27
        targetSdk = targetVersion
        versionCode = 26
        versionName = "1.$versionCode"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
        dataBinding = true
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue(type = "string", name = "app_name", value = "Tipuous debug")
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro",
            )
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.all {
            // Enable JUnit 5 platform for unit tests
            it.useJUnitPlatform()
        }
    }

    namespace = "com.tips.tipuous"

    packaging {
        jniLibs {
            excludes += listOf("META-INF/*")
        }
        resources {
            excludes += listOf("META-INF/*")
        }
    }
}

kotlin {
    jvmToolchain(21)
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.constraintlayout)
    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // Compose
    implementation(platform(libs.androidx.compose.bom)) // Added Compose BOM
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.ui)
    implementation(libs.activity.compose)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.text.google.fonts) // Added this line
    implementation(libs.androidx.material3) // Changed to material3
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.tooling.preview) // Changed to ui-tooling-preview
    debugImplementation(libs.androidx.ui.tooling) // Added debugImplementation for ui-tooling
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose) // For collectAsStateWithLifecycle
    implementation(libs.androidx.lifecycle.viewmodel.compose) // For viewModel() Composable
    implementation(libs.androidx.navigation.compose)      // For Compose Navigation


    // Styling
    implementation(libs.material)

    // Room
    implementation("androidx.room:room-runtime:2.6.1")
    implementation("androidx.room:room-ktx:2.6.1")
    ksp("androidx.room:room-compiler:2.6.1")

    // Databinding & other
    implementation(libs.androidx.legacy.support.v4)
    implementation(libs.androidx.navigation.ui.ktx)

    // Kotlin
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.kotlinx.serialization.json)

    // Feature module Support
    implementation(libs.androidx.navigation.dynamic.features.fragment)

    //Firebase
    // Import the BoM for the Firebase platform
    implementation(platform(libs.firebase.bom))

    // Add the dependencies for the Crashlytics and Analytics libraries
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.crashlytics)

    // Add the dependency for the Realtime Database library
    // When using the BoM, you don't specify versions in Firebase library dependencies
    implementation(libs.firebase.database)

    // Add the dependency for the Firebase SDK for Google Analytics
    implementation(libs.firebase.analytics)

    // Add Performance lib
    implementation(libs.firebase.perf)

    // In App Updates
    implementation(libs.app.update)
    // For Kotlin users also add the Kotlin extensions library for Play In-App Update:
    implementation(libs.app.update.ktx)

    // ML Kit OCR (on-device)
    implementation("com.google.mlkit:text-recognition:16.0.0")

    // Testing Navigation
    androidTestImplementation(libs.androidx.navigation.testing)

    // Testing
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly("org.junit.platform:junit-platform-launcher:1.13.4")

    // Testing - Core Library
    androidTestImplementation(libs.androidx.core)

    // Testing - AndroidJUnitRunner and JUnit Rules
    androidTestImplementation(libs.androidx.runner)
    androidTestImplementation(libs.androidx.rules)
    androidTestImplementation(libs.androidx.junit.ktx)

    // Testing - Assertions
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.truth)

    // Testing - Espresso
    androidTestImplementation(libs.androidx.espresso.core)

    // Test Orchestrator
    androidTestUtil(libs.androidx.orchestrator)
}