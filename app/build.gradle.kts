plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
    id("org.jetbrains.kotlin.android")
    id("org.jetbrains.kotlin.kapt")
    id("com.google.dagger.hilt.android")
}

android {
    namespace = "com.nilson.appsportmate"
    compileSdk = 35

    defaultConfig {
        applicationId = "com.nilson.appsportmate"
        minSdk = 24
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"

        // ✅ Runner para tests con Hilt
        testInstrumentationRunner = "com.nilson.appsportmate.HiltTestRunner"
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
        sourceCompatibility = JavaVersion.VERSION_21
        targetCompatibility = JavaVersion.VERSION_21
    }

    buildFeatures {
        viewBinding = true
    }

    // ✅ Necesario para tests instrumentados que acceden a recursos Android
    testOptions {
        unitTests.isIncludeAndroidResources = true
    }
}

// ✅ Fuerza versiones modernas y evita conflictos Firestore/Protobuf
configurations.all {
    resolutionStrategy {
        force("com.google.protobuf:protobuf-javalite:3.25.5")
        force("com.google.firebase:firebase-firestore:25.1.1")
    }
}

dependencies {
    // --- Jetpack + Material ---
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("androidx.activity:activity:1.8.2")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.android.material:material:1.11.0")

    // --- Firebase actualizado y alineado ---
    implementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    implementation("com.google.firebase:firebase-auth")
    implementation("com.google.firebase:firebase-firestore:25.1.1")
    implementation("com.google.firebase:firebase-storage")

    // --- Hilt / Navigation / Lifecycle ---
    implementation(libs.annotation)
    implementation(libs.lifecycle.livedata.ktx)
    implementation(libs.lifecycle.viewmodel.ktx)
    implementation(libs.navigation.fragment)
    implementation(libs.navigation.ui)
    implementation(libs.hilt.android)
    kapt(libs.google.hilt.compiler)

    // --- Glide ---
    implementation("com.github.bumptech.glide:glide:4.16.0")
    kapt("com.github.bumptech.glide:compiler:4.16.0")

    // --- Forzar protobuf moderno ---
    implementation("com.google.protobuf:protobuf-javalite:3.25.5")

    // ---------- TESTS UNITARIOS ----------
    testImplementation("junit:junit:4.13.2")
    testImplementation("org.mockito:mockito-core:5.7.0")
    testImplementation("androidx.arch.core:core-testing:2.2.0")
    testImplementation("org.jetbrains.kotlinx:kotlinx-coroutines-test:1.8.0")
    testImplementation("com.google.protobuf:protobuf-javalite:3.25.5")

    // ---------- TESTS INSTRUMENTADOS ----------
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    androidTestImplementation("androidx.test.espresso:espresso-contrib:3.5.1")
    androidTestImplementation("androidx.navigation:navigation-testing:2.7.7")
    androidTestImplementation("androidx.fragment:fragment-testing:1.7.1")

    // --- Firebase alineado también en tests ---
    androidTestImplementation(platform("com.google.firebase:firebase-bom:33.7.0"))
    androidTestImplementation("com.google.firebase:firebase-auth")
    androidTestImplementation("com.google.firebase:firebase-firestore:25.1.1")
    androidTestImplementation("com.google.protobuf:protobuf-javalite:3.25.5")

    // ---------- HILT TESTING ----------
    androidTestImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kaptAndroidTest("com.google.dagger:hilt-android-compiler:2.51.1")
    testImplementation("com.google.dagger:hilt-android-testing:2.51.1")
    kaptTest("com.google.dagger:hilt-android-compiler:2.51.1")
}
