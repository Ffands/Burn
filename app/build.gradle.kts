plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
}

android {
    namespace = "com.example.autoclicker"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.aistudio.applet.pjgekm"
        minSdk = 30
        targetSdk = 34
        versionCode = 8
        versionName = "1.1.1"
    }

    buildTypes {
        debug {
            isMinifyEnabled = false
        }
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    kotlinOptions {
        jvmTarget = "17"
    }
}

dependencies {
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("com.huawei.hms:ml-computer-vision-ocr:3.18.1.302")
    implementation("com.huawei.hms:ml-computer-vision-ocr-latin-model:3.18.1.302")
}
