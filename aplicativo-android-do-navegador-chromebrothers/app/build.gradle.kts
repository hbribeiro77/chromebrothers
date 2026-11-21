plugins {
    id("com.android.application")
    kotlin("android")
}

android {
    namespace = "com.chromebrothers.navegador"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.chromebrothers.navegador"
        minSdk = 29
        targetSdk = 35
        versionCode = 1
        versionName = "1.0"
        ndk {
            abiFilters += listOf("arm64-v8a")
        }
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }

    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }

    packaging {
        jniLibs {
            useLegacyPackaging = true
        }
    }
}

// Kotlin 2.3: o antigo `kotlinOptions { jvmTarget = "17" }` virou erro; este é o formato atual.
kotlin {
    compilerOptions {
        jvmTarget.set(org.jetbrains.kotlin.gradle.dsl.JvmTarget.JVM_17)
    }
}

dependencies {
    implementation(project(":regras-de-video-do-navegador"))
    implementation("androidx.core:core-ktx:1.15.0")
    implementation("androidx.documentfile:documentfile:1.0.1")
    implementation("androidx.appcompat:appcompat:1.7.0")
    implementation("com.google.android.material:material:1.12.0")
    implementation("androidx.constraintlayout:constraintlayout:2.2.0")
    implementation("androidx.recyclerview:recyclerview:1.3.0")
    // Mesma versão que o GeckoView 153 traz (senão o Gradle sobe sozinho e a 1.5.1 aqui engana).
    implementation("androidx.media3:media3-exoplayer:1.10.1")
    implementation("androidx.media3:media3-muxer:1.10.1")
    implementation("androidx.media3:media3-ui:1.10.1")
    // Recorte de trecho com "corte inteligente": só o começo (até o 1º quadro-chave) é reencodado.
    implementation("androidx.media3:media3-transformer:1.10.1")
    implementation("androidx.webkit:webkit:1.12.1")
    implementation("androidx.security:security-crypto:1.1.0-alpha06")
    implementation("com.squareup.okhttp3:okhttp:4.12.0")
    implementation("org.mozilla.geckoview:geckoview:153.0.20260810162159")
}
