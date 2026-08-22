@Suppress("DSL_SCOPE_VIOLATION")
plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.devtoolsKsp)
}

android {
    namespace = "com.example.emptyviewsactivity"
    compileSdk = 34

    defaultConfig {
        applicationId = "com.example.emptyviewsactivity"
        minSdk = 24
        targetSdk = 34
        versionCode = 1
        versionName = "1.0"

        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    buildTypes {
        release {
            isMinifyEnabled = false
        }
    }
    android {
        compileOptions {
            sourceCompatibility = JavaVersion.VERSION_17  // или VERSION_22
            targetCompatibility = JavaVersion.VERSION_17  // или VERSION_22
        }

        kotlin {
            jvmToolchain(17)  // или 22
        }
    }

    dependencies {
        implementation(libs.androidx.activity.ktx)
        implementation(libs.androidx.appcompat)
        implementation(libs.androidx.constraintlayout)
        implementation(libs.androidx.core.ktx)
        implementation(libs.material)

        // --- НОВЫЕ ЗАВИСИМОСТИ ДЛЯ ТВОЕГО ПРОЕКТА ---

        // 1. RecyclerView (чтобы список заметок рисовался)
        implementation(libs.androidx.recyclerview)

        // 2. Корутины (для Flow, StateFlow, viewModelScope)
        implementation(libs.kotlinx.coroutines.android)

        // 3. Lifecycle + ViewModel KTX (для stateIn, viewModelScope и т.д.)
        implementation(libs.androidx.lifecycle.viewmodel.ktx)
        implementation(libs.androidx.lifecycle.runtime.ktx)
        // --------------------------------------------

        testImplementation(libs.junit)
        androidTestImplementation(libs.androidx.espresso.core)
        androidTestImplementation(libs.androidx.junit)

        // Room (только KSP!)
        implementation(libs.room.runtime)
        implementation(libs.room.ktx)
        ksp(libs.room.compiler)
    }
}
