plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.jetbrains.kotlin.kapt)
}
apply("../manifest.gradle.kts")
android {
    namespace = "fan.akua.exam"
    compileSdk = extra["targetSdk"] as Int

    defaultConfig {
        applicationId = "fan.akua.exam"
        minSdk = extra["minSdk"] as Int
        targetSdk = extra["targetSdk"] as Int
        versionCode = extra["versionCode"] as Int
        versionName = extra["versionName"] as String

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            isDebuggable = false
            isProfileable = true
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
        buildConfig = true
    }
}

dependencies {
    implementation ("com.github.andrefrsousa:SuperBottomSheet:2.0.0")
    implementation (libs.lyricviewx)
    implementation(libs.library)
    implementation(libs.androidx.palette)
    debugImplementation(libs.leakcanary.android)
    implementation(libs.androidx.paging.runtime.ktx)

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