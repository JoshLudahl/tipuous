
buildscript {
    dependencies {
        classpath(libs.gradle)

                classpath(libs.google.services)
                classpath(libs.firebase.crashlytics.gradle)
                classpath(libs.perf.plugin)
    }
}

plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.kotlin.android) apply false
    alias(libs.plugins.ksp) apply false
}

apply(from = "ktlint.gradle")

tasks.register("clean", Delete::class) {
    delete(rootProject.layout.buildDirectory.get())
}