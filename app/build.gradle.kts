plugins {
    alias(libs.plugins.android.application)
}

android {
    namespace = "com.example.multipost"
    compileSdk = 37

    defaultConfig {
        applicationId = "com.example.multipost"
        minSdk = 24
        targetSdk = 37
        versionCode = 1
        versionName = "1.0"
    }

    packaging {
        resources {
            excludes += "META-INF/INDEX.LIST"
            excludes += "META-INF/DEPENDENCIES"
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
}

dependencies {

    implementation("androidx.core:core-ktx:1.17.0")
    implementation("androidx.appcompat:appcompat:1.7.1")
    implementation("androidx.activity:activity-ktx:1.11.0")
    implementation("androidx.fragment:fragment-ktx:1.8.9")
    implementation("androidx.constraintlayout:constraintlayout:2.2.1")
    implementation("com.google.android.material:material:1.14.0")

    // Media3
    implementation("androidx.media3:media3-exoplayer:1.8.0")
    implementation("androidx.media3:media3-ui:1.8.0")

    // Lifecycle
    implementation("androidx.lifecycle:lifecycle-runtime-ktx:2.9.3")

    // RecyclerView
    implementation("androidx.recyclerview:recyclerview:1.4.0")

    // WorkManager
    implementation("androidx.work:work-runtime-ktx:2.10.1")

    // Google authorization
    implementation("com.google.android.gms:play-services-auth:22.0.0")

    // Credential Manager
    implementation("androidx.credentials:credentials:1.5.0")
    implementation("androidx.credentials:credentials-play-services-auth:1.5.0")

    // Google API Client
    implementation("com.google.api-client:google-api-client-android:2.8.1")

    // YouTube Data API
    implementation("com.google.apis:google-api-services-youtube:v3-rev20260820-2.0.0")
    implementation("com.google.api-client:google-api-client-gson:2.8.1")
}