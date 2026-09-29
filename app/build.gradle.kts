import java.util.Properties
import org.jetbrains.kotlin.gradle.dsl.JvmTarget

plugins {
    alias(libs.plugins.android.application)
    alias(libs.plugins.kotlin.compose)
    alias(libs.plugins.kotlin.serialization)
    alias(libs.plugins.ksp)
    alias(libs.plugins.hilt)
    alias(libs.plugins.room)
}

// All build config comes from env/; the keys are listed in docs/setup.md.
// - version.properties: app version, committed
// - env.dev.properties / env.prod.properties: environment values per flavor, not secret, gitignored
// - key.properties: debug and release signing, secret, gitignored
val envDir: File = rootProject.file("env")

/** Values are trimmed: a stray trailing space or tab in a hand-edited file would otherwise break signing. */
fun loadEnvFile(name: String): Map<String, String> {
    val file: File = File(envDir, name)
    val properties: Properties = Properties()

    if (file.exists()) file.inputStream().use { properties.load(it) }
    return properties.stringPropertyNames().associateWith { properties.getProperty(it).trim() }
}

val keys: Map<String, String> = loadEnvFile("key.properties")

/** Keystore named by `<prefix>_STORE_FILE`: relative to env/, or to the repo root (e.g. env/release.jks). */
fun keystore(prefix: String): File? {
    val path: String = keys["${prefix}_STORE_FILE"]?.takeIf { it.isNotBlank() } ?: return null

    return listOf(File(envDir, path), rootProject.file(path)).firstOrNull { it.exists() }
}

val debugStoreFile: File? = keystore("DEBUG")
val releaseStoreFile: File? = keystore("RELEASE")

val versionProps: Map<String, String> = loadEnvFile("version.properties")
val appVersionName: String = versionProps["versionName"]?.takeIf { it.isNotBlank() }
    ?: error("env/version.properties has no versionName")
val appVersionCode: Int = versionProps["versionCode"]?.toIntOrNull()
    ?: error("env/version.properties has no numeric versionCode")

if (debugStoreFile == null) {
    logger.warn("env/key.properties has no usable DEBUG_STORE_FILE: debug builds use the default ~/.android key.")
}
if (releaseStoreFile == null) {
    logger.warn("env/key.properties has no usable RELEASE_STORE_FILE: release builds will be unsigned.")
}

/** Per-flavor values: env file -> BuildConfig.ENV, dev flag for the in-app tag, launcher label. */
fun com.android.build.api.dsl.ApplicationProductFlavor.configureEnvironment(flavor: String, appName: String, isDev: Boolean) {
    val env: Map<String, String> = loadEnvFile("env.$flavor.properties")

    if (env.isEmpty()) logger.warn("env/env.$flavor.properties is missing or empty: ENV falls back to \"$flavor\".")
    buildConfigField("String", "ENV", "\"${env["ENV"]?.takeIf { it.isNotBlank() } ?: flavor}\"")
    buildConfigField("boolean", "IS_DEV", isDev.toString())
    resValue("string", "app_name", appName)
}

android {
    namespace = "com.dd.sms.hook"
    compileSdk {
        version = release(37)
    }

    defaultConfig {
        applicationId = "com.dd.sms.hook"
        minSdk = 26
        targetSdk = 37
        versionCode = appVersionCode
        versionName = appVersionName

        testInstrumentationRunner = "com.dd.sms.hook.HiltTestRunner"
    }

    // dev installs next to prod (own id, "Dev" label, DEV tag in the app); each reads its own env file.
    flavorDimensions += "environment"
    productFlavors {
        create("dev") {
            dimension = "environment"
            applicationIdSuffix = ".dev"
            versionNameSuffix = "-dev"
            configureEnvironment(flavor = "dev", appName = "SMS Hook Dev", isDev = true)
        }
        create("prod") {
            dimension = "environment"
            configureEnvironment(flavor = "prod", appName = "SMS Hook", isDev = false)
        }
    }

    signingConfigs {
        if (debugStoreFile != null) {
            getByName("debug") {
                storeFile = debugStoreFile
                storePassword = keys["DEBUG_STORE_PASSWORD"]
                keyAlias = keys["DEBUG_KEY_ALIAS"]
                keyPassword = keys["DEBUG_KEY_PASSWORD"]
            }
        }
        if (releaseStoreFile != null) {
            create("release") {
                storeFile = releaseStoreFile
                storePassword = keys["RELEASE_STORE_PASSWORD"]
                keyAlias = keys["RELEASE_KEY_ALIAS"]
                keyPassword = keys["RELEASE_KEY_PASSWORD"]
            }
        }
    }

    buildTypes {
        debug {
            applicationIdSuffix = ".debug"
            versionNameSuffix = "-debug"
        }
        release {
            isMinifyEnabled = true
            isShrinkResources = true
            signingConfig = signingConfigs.findByName("release")
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
    androidResources {
        // Declares the supported languages to Android 13+ per-app language settings.
        generateLocaleConfig = true
    }
    buildFeatures {
        compose = true
        buildConfig = true
        resValues = true
    }
    testOptions {
        unitTests.isReturnDefaultValues = true
        unitTests.isIncludeAndroidResources = true
    }
    packaging {
        resources {
            excludes += "/META-INF/{AL2.0,LGPL2.1}"
        }
    }
}

kotlin {
    compilerOptions {
        jvmTarget.set(JvmTarget.JVM_17)
    }
}

room {
    schemaDirectory("$projectDir/schemas")
}

// Signed release builds copied to Release/ at the repo root, e.g. ./gradlew :app:exportProdReleaseAab.
// exportReleaseApk / exportReleaseAab are the prod shortcuts; tools/build_release_*.sh wrap them.
data class ReleaseFormat(val name: String, val buildTask: String, val outputDir: String, val extension: String)

val releaseFormats: List<ReleaseFormat> = listOf(
    ReleaseFormat(name = "Apk", buildTask = "assemble", outputDir = "outputs/apk/%s/release", extension = "apk"),
    ReleaseFormat(name = "Aab", buildTask = "bundle", outputDir = "outputs/bundle/%sRelease", extension = "aab"),
)

for (flavor in listOf("dev", "prod")) {
    val flavorTitle: String = flavor.replaceFirstChar { it.uppercase() }
    val flavorTag: String = if (flavor == "prod") "" else "-$flavor"

    for (format in releaseFormats) {
        val fileName: String = "sms-hook$flavorTag-$appVersionName-$appVersionCode.${format.extension}"

        tasks.register<Copy>("export${flavorTitle}Release${format.name}") {
            dependsOn("${format.buildTask}${flavorTitle}Release")
            from(layout.buildDirectory.dir(format.outputDir.format(flavor))) { include("*.${format.extension}") }
            into(rootProject.layout.projectDirectory.dir("Release"))
            // String overload, not a lambda: a lambda captures the build script and breaks the configuration cache.
            rename(".*\\.${format.extension}", fileName)
        }
    }
}
tasks.register("exportReleaseApk") { dependsOn("exportProdReleaseApk") }
tasks.register("exportReleaseAab") { dependsOn("exportProdReleaseAab") }

dependencies {
    // androidx core
    implementation(libs.androidx.core.ktx)
    implementation(libs.androidx.activity.compose)
    implementation(libs.androidx.appcompat)
    implementation(libs.androidx.lifecycle.runtime.compose)
    implementation(libs.androidx.lifecycle.viewmodel.compose)
    implementation(libs.androidx.lifecycle.service)
    implementation(libs.androidx.navigation.compose)
    // compose
    implementation(platform(libs.androidx.compose.bom))
    implementation(libs.androidx.compose.ui)
    implementation(libs.androidx.compose.ui.graphics)
    implementation(libs.androidx.compose.ui.tooling.preview)
    implementation(libs.androidx.compose.material3)
    implementation(libs.androidx.compose.material.icons.extended)
    debugImplementation(libs.androidx.compose.ui.tooling)
    debugImplementation(libs.androidx.compose.ui.test.manifest)
    // di
    implementation(libs.hilt.android)
    ksp(libs.hilt.compiler)
    implementation(libs.androidx.hilt.work)
    implementation(libs.androidx.hilt.lifecycle.viewmodel.compose)
    ksp(libs.androidx.hilt.compiler)
    // storage
    implementation(libs.androidx.room.runtime)
    implementation(libs.androidx.room.ktx)
    ksp(libs.androidx.room.compiler)
    implementation(libs.androidx.datastore.preferences)
    // background
    implementation(libs.androidx.work.runtime.ktx)
    // network + serialization
    implementation(libs.okhttp)
    implementation(libs.okhttp.logging)
    implementation(libs.kotlinx.serialization.json)
    implementation(libs.kotlinx.coroutines.android)
    // tests
    testImplementation(libs.junit)
    testImplementation(libs.mockk)
    testImplementation(libs.turbine)
    testImplementation(libs.kotlinx.coroutines.test)
    testImplementation(libs.okhttp.mockwebserver)
    testImplementation(libs.robolectric)
    testImplementation(libs.androidx.test.core.ktx)
    testImplementation(libs.androidx.navigation.testing)
    androidTestImplementation(libs.androidx.junit)
    androidTestImplementation(libs.androidx.test.runner)
    androidTestImplementation(libs.androidx.test.rules)
    androidTestImplementation(libs.androidx.test.core.ktx)
    androidTestImplementation(libs.androidx.espresso.core)
    androidTestImplementation(libs.androidx.room.testing)
    androidTestImplementation(libs.androidx.work.testing)
    androidTestImplementation(libs.hilt.android.testing)
    kspAndroidTest(libs.hilt.compiler)
    androidTestImplementation(libs.okhttp.mockwebserver)
    androidTestImplementation(libs.kotlinx.coroutines.test)
    androidTestImplementation(libs.turbine)
    androidTestImplementation(libs.mockk.android)
    androidTestImplementation(platform(libs.androidx.compose.bom))
    androidTestImplementation(libs.androidx.compose.ui.test.junit4)
}
