pluginManagement {
    resolutionStrategy {
        eachPlugin {
            when (requested.id.id) {
                "kotlin-ksp",
                "org.jetbrains.kotlin.kotlin-ksp",
                "org.jetbrains.kotlin.ksp" -> useModule("org.jetbrains.kotlin:kotlin-ksp:${requested.version}")
            }
        }
    }
    repositories {
        mavenLocal()
        google()
        gradlePluginPortal()
        mavenCentral()
        maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
}
rootProject.name = "simora"
