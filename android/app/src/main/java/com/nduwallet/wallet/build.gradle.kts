plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")

}

android {
    compileSdk = 32

    defaultConfig {
        applicationId = "com.nduwallet.wallet"
        minSdk = MIN_SDK
        targetSdk = 32
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
            signingConfig = signingConfigs.getByName("debug")
            proguardFiles(getDefaultProguardFile("proguard-android-optimize.txt"), "proguard-rules.pro")
        }
    }

    compileOptions {
        sourceCompatibility = jvmVersion
        targetCompatibility = jvmVersion
    }

    kotlinOptions {
        jvmTarget = jvmVersion.toString()
    }

    buildFeatures{
        dataBinding true // for data binding
        viewBinding true // for view binding
    }
    dataBinding{
        enabled = true
    }

}

dependencies {
    implementation(project(":sign"))
    implementation(project(":samples_common"))
    implementation 'androidx.fragment:fragment:1.2.4'
    scanner()
    glide_N_kapt()
    implementation("androidx.fragment:fragment-ktx:1.4.1")

    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.3")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.4.0")
}