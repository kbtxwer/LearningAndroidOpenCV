// Top-level build file where you can add configuration options common to all sub-projects/modules.
val kotlinVersion by extra("1.4.30")
buildscript {
    repositories {
        google()
        jcenter()
        maven(url = "https://jitpack.io")
    }
    dependencies {
        classpath("com.android.tools.build:gradle:4.2.1")
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.4.31")
        // NOTE: Do not place your application dependencies here; they belong
        // in the individual module build.gradle files
        classpath("androidx.navigation:navigation-safe-args-gradle-plugin:2.3.2")
    }
}

allprojects {
    repositories {
        google()
        jcenter()
        maven(url = "https://jitpack.io")
    }
}

tasks {
    val clean by registering(Delete::class) {
        delete(buildDir)
    }
}

//tasks.create<Delete>("clean") {
//    delete(buildDir)
//}

plugins {
    id("io.gitlab.arturbosch.detekt").version("1.16.0")
}

// root build.gradle
detekt {
    input = files("app/src/main/kotlin", "app/src/main/java") // 指定需要扫描的源代码文件路径
    config = files("config/detekt.yml") // 指定采用的规则集文件
    reports { // 指定输出的报告文件类型
        html {
            enabled = true// Enable/Disable HTML report (default: true)
            destination =
                file("build/reports/detekt.html") // Path where HTML report will be stored (default:
        }
    }
}
