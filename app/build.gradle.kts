plugins {
    id("com.android.application")
}

val releaseKeystorePath = System.getenv("AURELIUS_KEYSTORE_PATH")
val releaseKeystorePassword = System.getenv("AURELIUS_KEYSTORE_PASSWORD")
val releaseKeyAlias = System.getenv("AURELIUS_KEY_ALIAS")
val releaseKeyPassword = System.getenv("AURELIUS_KEY_PASSWORD")
val releaseVersionCode = System.getenv("AURELIUS_VERSION_CODE")?.toIntOrNull() ?: 1
val releaseVersionName = System.getenv("AURELIUS_VERSION_NAME") ?: "1.0"
val hasReleaseSigning =
    releaseKeystorePath != null &&
        releaseKeystorePassword != null &&
        releaseKeyAlias != null &&
        releaseKeyPassword != null

android {
    namespace = "com.donalgeraghty.stoicwidget"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.donalgeraghty.stoicwidget"
        minSdk = 26
        targetSdk = 37
        versionCode = releaseVersionCode
        versionName = releaseVersionName
    }

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseKeystorePath!!)
                storePassword = releaseKeystorePassword
                keyAlias = releaseKeyAlias
                keyPassword = releaseKeyPassword
            }
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
}
