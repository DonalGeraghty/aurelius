plugins {
    id("com.android.application")
}

val releaseKeystorePath = System.getenv("AURELIUS_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("AURELIUS_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("AURELIUS_KEY_ALIAS")
val releaseKeyPassword = System.getenv("AURELIUS_KEY_PASSWORD")

android {
    namespace = "com.donalgeraghty.stoicwidget"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.donalgeraghty.stoicwidget"
        minSdk = 26
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    signingConfigs {
        create("release") {
            if (
                releaseKeystorePath != null &&
                releaseKeystorePassword != null &&
                releaseKeyAlias != null &&
                releaseKeyPassword != null
            ) {
                storeFile = file(releaseKeystorePath)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("release")
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
