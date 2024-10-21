// Top-level build file where you can add configuration options common to all sub-projects/modules.
plugins {
    alias(libs.plugins.android.application) apply false
    alias(libs.plugins.jetbrains.kotlin.android) apply false
    alias(libs.plugins.google.gms.google.services) apply false
    id("org.jetbrains.kotlin.kapt") version "1.8.10" apply false
    id("com.google.dagger.hilt.android") version "2.48" apply false
}