import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.android.application)
    alias(libs.plugins.compose.multiplatform)
    alias(libs.plugins.compose.compiler)
    alias(libs.plugins.kotlin.serialization)
}

kotlin {
    androidTarget {
        compilerOptions {
            jvmTarget.set(JvmTarget.JVM_17)
        }
    }
    
    listOf(
        iosArm64(),
        iosSimulatorArm64()
    ).forEach { iosTarget ->
        iosTarget.binaries.framework {
            baseName = "ComposeApp"
            isStatic = true
        }
    }
    
    sourceSets {
        commonMain.dependencies {
            implementation(projects.feature.login)
            implementation(projects.feature.account)
            implementation(projects.feature.home)
            implementation(projects.feature.taxes)
            implementation(projects.feature.auto)
            implementation(projects.feature.notifications)
        }
        androidMain.dependencies {
            implementation(libs.androidx.core.splashscreen)
            implementation(libs.androidx.lifecycle.viewmodel.compose)
            implementation(libs.androidx.activity.compose)
            implementation(project.dependencies.platform(libs.firebase.bom))
            implementation(libs.firebase.messaging)
        }
        commonTest.dependencies {
            implementation(libs.kotlin.test)
        }
    }
}

android {
    namespace = "com.gmail.vanyadubik.myassistant"
    compileSdk = libs.versions.android.compileSdk.get().toInt()

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    buildFeatures {
        buildConfig = true
    }

    defaultConfig {
        applicationId = "com.gmail.vanyadubik.myassistant"
        minSdk = libs.versions.android.minSdk.get().toInt()
        targetSdk = libs.versions.android.targetSdk.get().toInt()
        versionCode = 101
        versionName = "1.0.1"

        buildConfigField("String", "FIREBASE_APP_ID", "\"1:81447438385:android:2cf1e7ece7e23b7745ed10\"")
        buildConfigField("String", "FIREBASE_API_KEY", "\"AIzaSyDu4pSNq8pKeFbVYjgGXJS1_pcFCT8E21I\"")
        buildConfigField("String", "FIREBASE_PROJECT_ID", "\"mytaxes-53780\"")
        buildConfigField("String", "FIREBASE_GCM_SENDER_ID", "\"81447438385\"")
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
    buildTypes {
        getByName("release") {
            isMinifyEnabled = true
            isShrinkResources = true
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            applicationIdSuffix = ".debug"
        }
    }
}

dependencies {
    debugImplementation(compose.uiTooling)
}

