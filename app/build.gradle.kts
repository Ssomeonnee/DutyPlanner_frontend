plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)

    alias(libs.plugins.hilt) // Добавляем плагин Hilt
    kotlin("kapt") // Добавляем плагин kapt
    id("org.jetbrains.kotlin.plugin.parcelize")
    //alias(libs.plugins.kotlin.compose)
}

android {
    namespace = "com.example.myapplication"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.example.myapplication"
        minSdk = 26
        targetSdk = 35
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
    buildFeatures {
        viewBinding = true
    }
    //buildFeatures {
        //compose = true
    //}
}

dependencies {

    implementation(libs.retrofit) // Или другую версию
    implementation(libs.converterGson) // Если используете Gson

    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.constraintlayout)
    implementation(libs.androidx.recyclerview)
    implementation(libs.google.material)
    implementation("com.google.android.material:material:1.11.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation(libs.androidx.navigation.fragment.ktx)
    implementation(libs.androidx.navigation.ui.ktx)

    //implementation(libs.androidx.activity.compose)
    //implementation(platform(libs.androidx.compose.bom))
    //implementation(libs.androidx.ui)
    //implementation(libs.androidx.ui.graphics)
    //implementation(libs.androidx.ui.tooling.preview)
    //implementation(libs.androidx.material3)

    //testImplementation(libs.junit)
    //androidTestImplementation(libs.androidx.junit)

    androidTestImplementation(libs.androidx.espresso.core)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)

    /*implementation(libs.hilt.android)
    kapt(libs.hilt.compiler)

    // Для навигации с Hilt (если используете)
    implementation(libs.hilt.navigation.compose)*/

    // Hilt
    implementation("com.google.dagger:hilt-android:2.48")
    kapt("com.google.dagger:hilt-android-compiler:2.48") //или annotationProcessor если kapt не используете

    //ViewModel
    implementation("androidx.lifecycle:lifecycle-viewmodel-ktx:2.8.0-alpha01")//или последняя версия

    //androidTestImplementation(platform(libs.androidx.compose.bom))
    //androidTestImplementation(libs.androidx.ui.test.junit4)
    //debugImplementation(libs.androidx.ui.tooling)
    //debugImplementation(libs.androidx.ui.test.manifest)
    testImplementation(kotlin("test"))


    // Добавленные зависимости для Espresso UI тестов
    androidTestImplementation(libs.espresso.core)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.espresso.contrib)
    androidTestImplementation(libs.espresso.intents)
}