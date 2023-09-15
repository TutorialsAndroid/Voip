import com.android.build.gradle.internal.cxx.configure.gradleLocalProperties

plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id ("kotlin-kapt")
}

val agoraAppID: String = gradleLocalProperties(rootDir).getProperty("agoraAppID")
val agoraAppCertificate: String = gradleLocalProperties(rootDir).getProperty("agoraAppCertificate")
android {
    namespace = "com.app.voip"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.app.voip"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0.0"

        android.buildFeatures.buildConfig = true

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        debug {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
        getByName("debug") {
            buildConfigField("String", "agoraAppID", agoraAppID)
            buildConfigField("String", "agoraAppCertificate", agoraAppCertificate)
        }
        getByName("release") {
            buildConfigField("String", "agoraAppID", agoraAppID)
            buildConfigField("String", "agoraAppCertificate", agoraAppCertificate)
        }
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        viewBinding = true
        dataBinding = true
    }
}

dependencies {

    //Agora SDK
    implementation("io.agora.rtc:voice-sdk:4.2.2")
    implementation ("commons-codec:commons-codec:1.15")

    //App Core libraries
    implementation("androidx.core:core-ktx:1.12.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.9.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("androidx.navigation:navigation-fragment-ktx:2.7.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.7.2")

    //Test implementation
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
}