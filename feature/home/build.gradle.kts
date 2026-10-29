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
            baseName = "homeKit"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.components.uiToolingPreview)
            api(libs.androidx.lifecycle.runtimeCompose)

            api(projects.core.resource)
            api(projects.core.domen)
            api(projects.core.network)
            api(projects.core.commonUi)
            api(projects.core.dataStore)
        }
        androidMain.dependencies {
            api(compose.preview)
            api(libs.androidx.activity.compose)
            api(libs.androidx.ui.tooling)
            api(libs.androidx.ui.tooling.preview)
        }
    }
}

android {
    namespace = "com.gmail.vanyadubik.myassistant.featurehome"
    compileSdk = libs.versions.android.compileSdk.get().toInt()
    defaultConfig {
        minSdk = libs.versions.android.minSdk.get().toInt()
    }

    buildFeatures {
        compose = true
    }

    composeOptions {
        kotlinCompilerExtensionVersion = libs.versions.composeMultiplatform.get()
    }
}