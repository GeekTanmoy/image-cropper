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
        sourceCompatibility = JavaVersion.VERSION_11
        targetCompatibility = JavaVersion.VERSION_11
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

    group = "com.github.GeekTanmoy"
    version = "1.0.0"
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
                groupId = "com.github.geektanmoy"
                artifactId = "image-cropper"
                version = "0.0.1"
            }
        }
    }
}