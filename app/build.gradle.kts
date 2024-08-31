plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)

}

android {
    namespace = "fan.akua.exam"
    compileSdk = 34

    defaultConfig {
        applicationId = "fan.akua.exam"
        minSdk = 24
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
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }
    buildFeatures {
        dataBinding = true
        viewBinding = true
    }
}

dependencies {
    implementation(libs.androidx.swiperefreshlayout)
    implementation(libs.lottie)
    implementation(libs.refresh.layout.kernel)
    implementation(libs.refresh.header.radar)
    implementation(libs.glide)
    implementation(libs.banner)
    implementation(libs.brv)
    implementation(libs.spannable)
    implementation(libs.androidx.activity.ktx)
    implementation(libs.androidx.fragment.ktx)
    implementation(libs.converter.gson)
    implementation(libs.retrofit)
    implementation(libs.mmkv)
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}