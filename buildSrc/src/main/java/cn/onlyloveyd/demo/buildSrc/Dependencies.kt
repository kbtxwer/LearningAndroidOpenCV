package cn.onlyloveyd.demo.buildSrc

object Dependencies {

    const val compileSdkVersion = 30
    const val buildToolsVersion = "30.0.2"
    const val minSdkVersion = 21
    const val targetSdkVersion = 30
    const val ndkVersion = "20.0.5594570"

    const val kotlinVersion = "1.5.21"

    object Kotlin {
        const val kotlinStdLib = "org.jetbrains.kotlin:kotlin-stdlib:$kotlinVersion"
        const val coroutines = "org.jetbrains.kotlinx:kotlinx-coroutines-android:1.5.0"
    }

    object AndroidX {
        const val coreKtx = "androidx.core:core-ktx:1.5.0"
        const val appcompat = "androidx.appcompat:appcompat:1.3.0"
        const val constraintLayout = "androidx.constraintlayout:constraintlayout:2.0.4"
        const val recyclerview = "androidx.recyclerview:recyclerview:1.2.0"
        const val viewpager2 = "androidx.viewpager2:viewpager2:1.0.0"
        const val cardview = "androidx.cardview:cardview:1.0.0"
        const val activityKtx = "androidx.activity:activity-ktx:1.3.0-alpha06"
        const val fragmentKtx = "androidx.fragment:fragment-ktx:1.3.5"
    }

    const val navVersion = "2.3.4"

    object Navigation {
        const val navigationUiKtx = "androidx.navigation:navigation-fragment-ktx:$navVersion"
        const val navigationFragmentKtx = "androidx.navigation:navigation-ui-ktx:$navVersion"
    }

    const val cameraXVersion = "1.0.0-rc05"

    object CameraX {
        const val cameraCore = "androidx.camera:camera-core:${cameraXVersion}"
        const val cameraCamera2 = "androidx.camera:camera-camera2:${cameraXVersion}"
        const val cameraLifecycle = "androidx.camera:camera-lifecycle:${cameraXVersion}"
        const val cameraView = "androidx.camera:camera-view:1.0.0-alpha24"
        const val cameraExtensions = "androidx.camera:camera-extensions:1.0.0-alpha24"
    }

    object Google {
        const val material = "com.google.android.material:material:1.3.0"
        const val ksp = "com.google.devtools.ksp:symbol-processing-api:1.5.21-1.0.0-beta05"
    }

    object Github {
        const val MPAndroidChart = "com.github.PhilJay:MPAndroidChart:v3.1.0"
        const val Glide = "com.github.bumptech.glide:glide:4.12.0"
        const val EasyPhotos = "com.github.HuanTanSheng:EasyPhotos:3.1.3"
        const val SmartCrop = "com.github.pqpo:SmartCropper:v2.1.3"
    }

    object Tencent {
        const val mmkv = "com.tencent:mmkv-static:1.2.7"
    }

    object AnnotationProcessor {
        const val Glide = "com.github.bumptech.glide:compiler:4.12.0"
    }

}
