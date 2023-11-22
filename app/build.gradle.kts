plugins {
    id("com.android.application")
    id("org.jetbrains.kotlin.android")
    id("com.google.gms.google-services")

}

android {
    namespace = "com.example.taller3"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.taller3"
        minSdk = 24
        targetSdk = 33
        versionCode = 1
        versionName = "1.0"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildFeatures{
        viewBinding = true
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
}

dependencies {

    implementation("androidx.core:core-ktx:1.9.0")
    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("org.osmdroid:osmdroid-android:6.1.17")
    implementation("org.slf4j:slf4j-android:1.7.32")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.github.MKergall:osmbonuspack:6.8.0")
    implementation("androidx.preference:preference-ktx:1.2.1")
    implementation("com.squareup.okhttp3:logging-interceptor:3.8.1")
    implementation("com.github.parse-community.Parse-SDK-Android:parse:1.26.0")
    implementation("com.github.parse-community:ParseLiveQuery-Android:1.2.2")
    implementation("com.mikhaellopez:circularprogressbar:3.1.0")
    implementation("com.makeramen:roundedimageview:2.3.0")
    implementation("com.google.firebase:firebase-storage:20.3.0")
    implementation("androidx.navigation:navigation-fragment-ktx:2.5.3")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.2")
    implementation("androidx.navigation:navigation-ui-ktx:2.5.3")
    implementation("com.google.firebase:firebase-storage-ktx:20.3.0")
    implementation ("com.github.bumptech.glide:glide:4.12.0")
    implementation ("de.hdodenhof:circleimageview:3.1.0")
    annotationProcessor ("com.github.bumptech.glide:compiler:4.12.0")
    testImplementation("junit:junit:4.13.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")

}