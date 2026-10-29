plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.library)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {

    androidTarget()

    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "commonuiKit"
        }
    }

    sourceSets {
        commonMain.dependencies {
            api(compose.foundation)
            api(compose.ui)
            api(compose.material)
            api(compose.material3)
            api(compose.animation)
            api(compose.runtime)
            api(compose.runtimeSaveable)
            api(compose.components.resources)
            api(compose.components.uiToolingPreview)
            api(compose.materialIconsExtended)
            api(libs.navigation.compose)

            api(libs.compose.ui.backhandler)

            api(libs.koin.compose)
            api(libs.koin.compose.viewmodel)

            api(libs.coil.compose)
            api(libs.coil.network.ktor)

            api(libs.kotlinx.datetime)

            api(projects.core.resource)
            api(projects.core.domen)
            api(projects.core.network)

        }
        androidMain.dependencies {
            api(compose.preview)
            api(libs.androidx.activity.compose)
            api(libs.androidx.navigation.compose)
            api(libs.androidx.ui.tooling)
            api(libs.androidx.ui.tooling.preview)
        }
    }
}

android {
    namespace = "com.gmail.vanyadubik.myassistant.commonui"
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