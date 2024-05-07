plugins {
    id("com.android.application")
    id("com.google.gms.google-services")
}

android {
    namespace = "com.example.safetyapp"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.safetyapp"
        minSdk = 24
        targetSdk = 33
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
    buildFeatures {
        viewBinding = true
    }
    testOptions.unitTests {
        isIncludeAndroidResources = true
    }
}

dependencies {

    implementation("androidx.appcompat:appcompat:1.6.1")
    implementation("com.google.android.material:material:1.10.0")
    implementation("androidx.constraintlayout:constraintlayout:2.1.4")
    implementation("com.google.firebase:firebase-auth:22.3.0")
    implementation("com.google.firebase:firebase-database:20.3.0")
    implementation("androidx.cardview:cardview:1.0.0")
    implementation("com.google.android.gms:play-services-maps:18.2.0")
    implementation("com.google.android.gms:play-services-location:21.0.1")
    implementation("com.karumi:dexter:6.2.1")
    implementation("androidx.navigation:navigation-fragment:2.7.5")
    implementation("androidx.navigation:navigation-ui:2.7.5")
    testImplementation("junit:junit:4.13.2")
    testImplementation ("org.mockito:mockito-core:3.12.4")
    testImplementation ("org.robolectric:robolectric:4.7.1")
    implementation ("androidx.fragment:fragment:1.6.2")
    androidTestImplementation("androidx.test.ext:junit:1.1.5")
    androidTestImplementation("com.android.support.test:runner:1.0.2")
    androidTestImplementation("androidx.test.espresso:espresso-core:3.5.1")
    testImplementation ("androidx.test:core:1.5.0")

}