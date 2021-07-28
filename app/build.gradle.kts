import cn.onlyloveyd.demo.buildSrc.Dependencies

plugins {
    id("com.android.application")
    kotlin("android")
    kotlin("kapt")
    id("androidx.navigation.safeargs.kotlin")
}

android {
    compileSdkVersion(Dependencies.compileSdkVersion)
    buildToolsVersion(Dependencies.buildToolsVersion)
    ndkVersion = Dependencies.ndkVersion
    defaultConfig {
        applicationId = "cn.onlyloveyd.demo"
        minSdkVersion(Dependencies.minSdkVersion)
        targetSdkVersion(Dependencies.targetSdkVersion)
        versionCode(1)
        versionName("1.0")

        externalNativeBuild {
            cmake {
                cppFlags("-std=c++11", "-frtti", "-fexceptions -lz")
            }
        }
    }
    buildTypes {
        named("release") {
            isMinifyEnabled = false
            proguardFiles(
                getDefaultProguardFile("proguard-android-optimize.txt"),
                "proguard-rules.pro"
            )
        }
    }
    externalNativeBuild {
        cmake {
            path("src/main/cpp/CMakeLists.txt")
            version = "3.10.2"
        }
    }
    buildFeatures {
        dataBinding = true
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_1_8
        targetCompatibility = JavaVersion.VERSION_1_8
    }
    kotlinOptions {
        jvmTarget = "1.8"
    }

    // 不压缩模型
    aaptOptions {
        noCompress("tflite", "lite")
    }
}

dependencies {
    implementation(fileTree(mapOf("dir" to "libs", "include" to listOf("*.jar"))))

    //    implementation(project(path = ":sdk450"))
    implementation(project(path = ":sdk452"))

    Dependencies.Kotlin.run {
        implementation(kotlinStdLib)
        implementation(cn.onlyloveyd.demo.buildSrc.Dependencies.Kotlin.coroutines)
    }

    Dependencies.AndroidX.run {
        implementation(coreKtx)
        implementation(appcompat)
        implementation(constraintLayout)
        implementation(recyclerview)
        implementation(viewpager2)
        implementation(cardview)
        implementation(activityKtx)
        implementation(fragmentKtx)
    }

    Dependencies.Navigation.run {
        implementation(navigationUiKtx)
        implementation(navigationFragmentKtx)
    }

    Dependencies.CameraX.run {
        implementation(cameraCore)
        implementation(cameraCamera2)
        implementation(cameraLifecycle)
        implementation(cameraView)
        implementation(cameraExtensions)
    }

    Dependencies.Google.run {
        implementation(material)
        implementation(ksp)
    }

    Dependencies.Github.run {
        implementation(MPAndroidChart)
        implementation(Glide)
        implementation(EasyPhotos)
        implementation(SmartCrop)
    }

    Dependencies.Tencent.run {
        implementation(mmkv)
    }

    Dependencies.AnnotationProcessor.run {
        annotationProcessor(Glide)
    }

}
