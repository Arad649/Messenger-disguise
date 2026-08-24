plugins {
    id("com.android.application")
}

android {
    namespace = "com.arad.settingschat"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.arad.settingschat"
        minSdk = 26
        targetSdk = 35
        versionCode = 5
        versionName = "2.3.0"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
