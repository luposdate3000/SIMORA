import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
    }
    dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.6.0")
    }
}
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("org.jetbrains.kotlin.multiplatform") version "1.6.0"
}
repositories {
    mavenLocal()
    google()
    mavenCentral()
}
group = "simora"
version = "0.0.1"
apply(plugin = "maven-publish")
kotlin {
    explicitApi()
    metadata {
        compilations.forEach {
            it.kotlinOptions {
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
                freeCompilerArgs += "-Xnew-inference"
                freeCompilerArgs += "-Xinline-classes"
            }
        }
    }
    jvm {
        compilations.forEach {
            it.kotlinOptions {
                jvmTarget = "1.8"
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
                freeCompilerArgs += "-Xno-param-assertions"
                freeCompilerArgs += "-Xnew-inference"
                freeCompilerArgs += "-Xno-receiver-assertions"
                freeCompilerArgs += "-Xno-call-assertions"
            }
        }
    }
    linuxX64("linuxX64") {
        compilations.forEach {
            it.kotlinOptions {
                freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
                freeCompilerArgs += "-Xnew-inference"
            }
        }
        binaries {
            sharedLib(listOf(RELEASE)) {
                baseName = "simora_release"
            }
            sharedLib(listOf(DEBUG)) {
                baseName = "simora_debug"
            }
            executable(listOf(RELEASE, DEBUG)) {
            }
        }
    }
    js {
        moduleName = "simora"
        browser {
            compilations.forEach {
                it.kotlinOptions {
                    freeCompilerArgs += "-Xopt-in=kotlin.RequiresOptIn"
                    freeCompilerArgs += "-Xnew-inference"
                }
            }
            dceTask {
//                keep("Luposdate3000_Endpoint.lupos.endpoint.LuposdateEndpoint")
            }
            testTask {
                useKarma {
                    useFirefox()
                }
            }
        }
        binaries.executable()
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.2.1")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        val jvmMain by getting {
            dependencies {
            }
        }
        val desktopMain by creating {
            dependsOn(commonMain)
        }
        val linuxX64Main by getting {
            dependsOn(desktopMain)
        }
//        val mingwX64Main by getting {
//            dependsOn(desktopMain)
//        }
//        val macosX64Main by getting {
//            dependsOn(desktopMain)
//        }
        val jvmTest by getting {
            dependencies {
                implementation(kotlin("test"))
                implementation(kotlin("test-junit"))
            }
        }
        val jsMain by getting {
            dependencies {
            }
        }
    }
}
tasks.register("luposSetup") {
    fun fixPathNames(s: String): String {
        var res = s.trim()
        var back = ""
        while (back != res) {
            back = res
            res = res.replace("\\", "/").replace("/./", "/").replace("//", "/")
        }
        return res
    }

    val regexDisableNoInline = "(^|[^a-zA-Z])noinline ".toRegex()
    val regexDisableInline = "(^|[^a-zA-Z])inline ".toRegex()
    val regexDisableCrossInline = "(^|[^a-zA-Z])crossinline ".toRegex()
    val bp = File(buildDir.parentFile, "/src")
    for (it in bp.walk()) {
        val tmp = it.toString()
        val ff = File(tmp)
        if (ff.isFile && ff.name.endsWith(".kt")) {
            File(ff.absolutePath + ".tmp").printWriter().use { out ->
                var line = 0
                ff.forEachLine { line2 ->
                    var s = line2
                    s = s.replace("SOURCE_FILE_START.*SOURCE_FILE_END".toRegex(), "SOURCE_FILE_START*/\"${fixPathNames(ff.absolutePath)}:$line\"/*SOURCE_FILE_END")
                    s = s.replace("/*NOINLINE*/", "noinline ")
                    s = s.replace("/*CROSSINLINE*/", "crossinline ")
                    s = s.replace("/*INLINE*/", "inline ")
                    out.println(s)
                    line++
                }
            }
            File(ff.absolutePath + ".tmp").copyTo(ff, true)
            File(ff.absolutePath + ".tmp").delete()
        }
    }
}
tasks.named("generateProjectStructureMetadata") {
    dependsOn("luposSetup")
}
tasks.named("compileKotlinJvm") {
    dependsOn("luposSetup")
    doLast {
        File(buildDir, "external_jvm_dependencies").printWriter().use { out ->
            for (f in configurations.getByName("jvmRuntimeClasspath").resolve()) {
                out.println("$f")
            }
        }
    }
}
tasks.named("compileKotlinJs") {
    dependsOn("luposSetup")
    doLast {
        File(buildDir, "external_js_dependencies").printWriter().use { out ->
            for (f in configurations.getByName("jsRuntimeClasspath").resolve()) {
                out.println("$f")
            }
        }
    }
}
tasks.named("build") {
}
tasks.withType<Test> {
    maxHeapSize = "1g"
    maxParallelForks = 20
    testLogging {
        exceptionFormat = TestExceptionFormat.FULL
        showStandardStreams = true
        events.add(TestLogEvent.STARTED)
        events.add(TestLogEvent.FAILED)
        events.add(TestLogEvent.PASSED)
        events.add(TestLogEvent.SKIPPED)
        events.add(TestLogEvent.STANDARD_OUT)
        events.add(TestLogEvent.STANDARD_ERROR)
    }
}
configure<org.jlleitschuh.gradle.ktlint.KtlintExtension> {
    enableExperimentalRules.set(true)
    ignoreFailures.set(true)
    filter {
        exclude("**/build/**")
    }
}
