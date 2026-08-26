import com.android.build.api.dsl.ApplicationExtension
import org.jlleitschuh.gradle.ktlint.reporter.ReporterType

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.services)
    alias(libs.plugins.firebase.crashlytics)
    alias(libs.plugins.firebase.perf)
    alias(libs.plugins.ktlint)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serializable)
}

configure<ApplicationExtension> {
    val targetVersion = 37

    namespace = "com.tips.tipuous"

    compileSdk = targetVersion

    defaultConfig {
        applicationId = "com.tips.tipuous"
        minSdk = 29
        targetSdk = targetVersion
        versionCode = 37
        versionName = "2026.08.26"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        buildConfig = true
        compose = true
    }

    buildTypes {
        release {
            optimization {
                enable = true
            }
        }
        debug {
            optimization {
                enable = false
            }
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            resValue(type = "string", name = "app_name", value = "Tipuous debug")
        }
    }

    testOptions {
        execution = "ANDROIDX_TEST_ORCHESTRATOR"
        unitTests.all {
            // Enable JUnit 5 platform for unit tests
            it.useJUnitPlatform()
        }
    }

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

ktlint {
    android = true
    ignoreFailures = false
    reporters {
        reporter(ReporterType.CHECKSTYLE)
        reporter(ReporterType.JSON)
        reporter(ReporterType.HTML)
    }
    additionalEditorconfig.set(
        mapOf(
            "max_line_length" to "off",
            "ktlint_function_naming_ignore_when_annotated_with" to "Composable",
        ),
    )
}

tasks.named("preBuild") {
    dependsOn("ktlintFormat")
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    implementation(libs.lifecycle.extensions)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.constraintlayout)
    implementation(libs.gridlayout)

    // Compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.core.ktx)
    implementation(libs.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.ui)
    implementation(libs.activity.compose)
    implementation(libs.androidx.ui.text)
    implementation(libs.androidx.ui.text.google.fonts)
    implementation(libs.androidx.material3)
    implementation(libs.androidx.material3.android)
    implementation(libs.androidx.ui.tooling.preview)
    debugImplementation(libs.androidx.ui.tooling)
    implementation(libs.androidx.material.icons.extended)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.navigation3.ui)
    implementation(libs.androidx.navigation3.runtime)
    implementation(libs.androidx.lifecycle.viewmodel.navigation3)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Data-binding & other
    implementation(libs.androidx.legacy.support.v4)

    // Kotlin
    implementation(libs.kotlinx.serialization.json)

    // Feature module Support
    // Firebase
    implementation(platform(libs.firebase.bom))
    implementation(libs.firebase.crashlytics)
    implementation(libs.firebase.database)
    implementation(libs.firebase.analytics)
    implementation(libs.firebase.perf)

    // IAU
    implementation(libs.app.update)
    implementation(libs.app.update.ktx)

    // ML Kit OCR (on-device)
    implementation(libs.text.recognition)

    // Testing
    testImplementation(libs.junit.jupiter.api)
    testRuntimeOnly(libs.junit.jupiter.engine)
    testRuntimeOnly(libs.junit.platform.launcher)

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
