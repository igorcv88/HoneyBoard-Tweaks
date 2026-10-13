plugins { id("com.android.application") }
android {
    namespace = "io.github.igorcv88.honeyboardtweaks"
    compileSdk { version = release(37) }
    defaultConfig {
        applicationId = "io.github.igorcv88.honeyboardtweaks"
        minSdk = 33
        targetSdk = 36
        versionCode = 1
        versionName = "0.1.0"
    }
    compileOptions {
        sourceCompatibility = JavaVersion.VERSION_17
        targetCompatibility = JavaVersion.VERSION_17
    }
    buildTypes { release { isMinifyEnabled = false } }
}
dependencies {
    compileOnly("io.github.libxposed:api:102.0.0")
    implementation("io.github.libxposed:service:102.0.0")
    testImplementation("junit:junit:4.13.2")
}
