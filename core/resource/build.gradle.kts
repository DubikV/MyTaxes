plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
}

kotlin {

    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "resourceKit"
        }
    }

    sourceSets {
        commonMain {
            dependencies {
                api(compose.runtime)
                api(compose.foundation)
                api(compose.components.resources)
                api(compose.ui)
            }
        }
    }
}

compose.resources {
    publicResClass = true //access in another modules
    packageOfResClass = "com.gmail.vanyadubik.myassistant.resource"
    generateResClass = auto
}

android {
    namespace = "com.gmail.vanyadubik.myassistant.resource"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }
}