plugins {
    alias(libs.plugins.android.library)

    id("maven-publish") //For Maven Publish (Optional)
}

android {
    namespace = "com.geektanmoy.imagecropper"
    compileSdk {
        version = release(36)
    }

    defaultConfig {
        minSdk = 26

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
        consumerProguardFiles("consumer-rules.pro")
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
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildFeatures {
        viewBinding = true
    }

    //Maven Publishing
    publishing {
        singleVariant("release") {
            withSourcesJar()
            withJavadocJar()
        }
    }
}

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.appcompat)
    implementation(libs.material)
    implementation(libs.androidx.activity)
    implementation(libs.androidx.constraintlayout)
    testImplementation(libs.junit)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.espresso.core)
}

//Maven Local Script
publishing {
    publications{
        register<MavenPublication>("release"){
            afterEvaluate {
                from(components["release"])
                groupId = "com.github.GeekTanmoy"
                artifactId = "image-cropper"
                version = "0.0.1"
            }
        }
    }
}