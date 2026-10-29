plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "networkKit"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(libs.ktor.core)
            api(libs.ktor.json)
            api(libs.ktor.serialization)
            api(libs.ktor.negotiation)
            api(libs.ktor.serialization)
            api(libs.ktor.kotlinx.json)
            api(libs.ktor.logging)
            api(projects.core.domen)
        }
        androidMain.dependencies {
            api(libs.ktor.okhttp)
            api(libs.ktor.android)
            api(libs.ktor.utils.jvm)
        }
        iosMain.dependencies {
            api(libs.ktor.ios)
        }
    }
}

android {
    namespace = "com.gmail.vanyadubik.myassistant.network"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}