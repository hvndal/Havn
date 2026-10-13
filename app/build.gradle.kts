import java.util.Properties

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.android)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.hilt)
    alias(libs.plugins.ksp)
    alias(libs.plugins.kotlin.serialization)
}

android {
    namespace = "com.havn.app"
    compileSdk = 36

    defaultConfig {
        applicationId = "com.havnmeds.app"
        minSdk = 26
        targetSdk = 36
        versionCode = 21
        versionName = "1.1.3"
        testInstrumentationRunner = "androidx.test.runner.AndroidJUnitRunner"
    }

    /**
     * Release signing is read from `keystore.properties` (gitignored) or from
     * the environment, so no credential ever lands in version control.
     *
     * If neither is present the release build is left **unsigned** rather than
     * falling back to a debug key. That is the important part: this project
     * previously signed release with the debug keystore, whose password is the
     * literal string "android" and is documented publicly. Anyone could have
     * modified the APK, re-signed it with the same well-known key, and Android
     * would have accepted it as a legitimate update from this developer — on an
     * app that holds medication schedules.
     */
    val keystoreProps = Properties().apply {
        val f = rootProject.file("keystore.properties")
        if (f.exists()) f.inputStream().use { load(it) }
    }

    fun prop(name: String, env: String): String? =
        keystoreProps.getProperty(name) ?: System.getenv(env)

    val releaseStorePath = prop("storeFile", "HAVN_KEYSTORE")
    val hasReleaseSigning = releaseStorePath != null && file(releaseStorePath).exists()

    signingConfigs {
        if (hasReleaseSigning) {
            create("release") {
                storeFile = file(releaseStorePath!!)
                storePassword = prop("storePassword", "HAVN_KEYSTORE_PASSWORD")
                keyAlias = prop("keyAlias", "HAVN_KEY_ALIAS")
                keyPassword = prop("keyPassword", "HAVN_KEY_PASSWORD")
            }
        }
    }

    buildTypes {
        debug {
            // No explicit signingConfig: AGP's built-in debug key is used, which
            // every developer already has. Pointing this at a keystore committed
            // beside the source only created something that looked like a secret.
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
            isMinifyEnabled = false
        }
        release {
            if (hasReleaseSigning) {
                signingConfig = signingConfigs.getByName("release")
            }
            ndk {
                debugSymbolLevel = "FULL"
            }
            // R8 on: shrinks the APK and strips readable class and method names.
            isMinifyEnabled = true
            isShrinkResources = true
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

    kotlinOptions {
        jvmTarget = "17"
    }

    buildFeatures {
        compose = true
        // BackupManager stamps the app version into every export.
        buildConfig = true
    }

    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }

    testOptions {
        // Robolectric launch tests boot the real app, resources and all.
        unitTests.isIncludeAndroidResources = true
    }

    lint {
        abortOnError = false
        checkDependencies = true
    }
}

/**
 * Regression guard for the v20 cold-start crash ("CompositionLocal
 * LocalLifecycleOwner not present"). Lifecycle 2.8+ reads its own
 * LocalLifecycleOwner, which only Compose UI 1.7+ provides. Any build where
 * lifecycle artifacts resolve to more than one version fails here, before it
 * can reach users.
 */
configurations.configureEach {
    if (name.endsWith("RuntimeClasspath")) {
        incoming.afterResolve {
            val versions = resolutionResult.allComponents
                .mapNotNull { it.id as? org.gradle.api.artifacts.component.ModuleComponentIdentifier }
                .filter { it.group == "androidx.lifecycle" }
                .map { it.version }
                .toSet()
            if (versions.size > 1) {
                throw GradleException("androidx.lifecycle versions are misaligned in $name: $versions")
            }
        }
    }
}

/** The deprecated Compose UI LocalLifecycleOwner silently diverges from lifecycle's own; ban it. */
val checkNoDeprecatedLifecycleOwner by tasks.registering {
    val sources = fileTree("src") { include("**/*.kt") }
    inputs.files(sources)
    doLast {
        val bad = sources.files.filter {
            it.readText().contains("androidx.compose.ui.platform.LocalLifecycleOwner")
        }
        if (bad.isNotEmpty()) {
            throw GradleException(
                "Use androidx.lifecycle.compose.LocalLifecycleOwner instead of the deprecated one in:\n" +
                    bad.joinToString("\n")
            )
        }
    }
}
tasks.named("preBuild") { dependsOn(checkNoDeprecatedLifecycleOwner) }

dependencies {
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.lifecycle.runtime.ktx)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.splashscreen)

    // Compose BOM
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.ui)
    implementation(libs.androidx.ui.graphics)
    implementation(libs.androidx.ui.tooling.preview)
    implementation(libs.androidx.material3)

    // Navigation
    implementation(libs.androidx.navigation.compose)

    // Room
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)

    // Hilt
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.hilt.navigation.compose)
    implementation(libs.hilt.work)
    ksp(libs.hilt.work.compiler)

    // WorkManager
    implementation(libs.androidx.work.runtime.ktx)

    // DataStore
    implementation(libs.androidx.datastore.preferences)

    // Glance Widgets
    implementation(libs.androidx.glance.appwidget)
    implementation(libs.androidx.glance.material3)

    // Kotlinx
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)

    // Testing
    testImplementation(libs.junit)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core)
    testImplementation(libs.androidx.junit)
    testImplementation(platform(libs.androidx.compose.bom))
    testImplementation(libs.androidx.ui.test.junit4)
    debugImplementation(libs.androidx.ui.test.manifest)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.core)
    androidTestImplementation(libs.androidx.espresso.core)
    debugImplementation(libs.androidx.ui.tooling)
}
