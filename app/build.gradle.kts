plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.google.gms.google.services)
    id ("kotlin-kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    namespace = "com.example.thecube"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.thecube"
        minSdk = 27
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

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
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
    }
    kotlinOptions {
        jvmTarget = "11"
    }

    buildFeatures{
        viewBinding = true
    }
}

dependencies {

    implementation(libs.androidx.room.ktx)     // Room coroutine and LiveData support
    implementation (libs.androidx.room.runtime)
    implementation(libs.firebase.database.ktx)
    implementation (libs.material.v190)
    implementation(libs.firebase.auth.ktx)
    kapt (libs.androidx.room.compiler)
    implementation (libs.androidx.lifecycle.livedata.ktx)
    implementation (libs.androidx.lifecycle.viewmodel.ktx.v260)
    implementation(platform(libs.firebase.bom))
    implementation(libs.google.firebase.firestore)
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.ktx)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.firebase.firestore)
    implementation (libs.material.v170)
    implementation (libs.androidx.material3)
    implementation(libs.glide)
    kapt(libs.compiler)
    implementation(libs.retrofit)
    implementation(libs.converter.gson)


    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}