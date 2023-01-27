import org.gradle.api.tasks.testing.logging.TestExceptionFormat
import org.gradle.api.tasks.testing.logging.TestLogEvent
val compileLinux: Boolean = true
val compileOSX: Boolean = false
val compileWindows: Boolean = false
val compileJS: Boolean = true
val compileJVM: Boolean = true

val nodeJSMode = true

buildscript {
    repositories {
        mavenLocal()
        google()
        mavenCentral()
maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
    }
dependencies {
        classpath("org.jetbrains.kotlin:kotlin-gradle-plugin:1.8.0")
    }
}
plugins {
    id("org.jlleitschuh.gradle.ktlint") version "10.1.0"
    id("org.jetbrains.kotlin.multiplatform") version "1.8.0"
    `maven-publish`
}
repositories {
    mavenLocal()
    google()
    mavenCentral()
maven { url = uri("https://oss.sonatype.org/content/repositories/snapshots/") }
}
group = "simora"
version = "0.0.1"
kotlin {
    explicitApi()
    metadata {
        compilations.forEach {
            it.kotlinOptions {
                freeCompilerArgs += "-Xnew-inference"
                freeCompilerArgs += "-Xinline-classes"
            }
        }
    }
    if (compileJVM) {
        jvm {
            compilations.forEach {
                it.kotlinOptions {
                    jvmTarget = "1.8"
                    freeCompilerArgs += "-Xno-param-assertions"
                    freeCompilerArgs += "-Xnew-inference"
                    freeCompilerArgs += "-Xno-receiver-assertions"
                    freeCompilerArgs += "-Xno-call-assertions"
                }
            }
        }
    }
    if (compileLinux) {
        linuxX64("linuxX64") {
            compilations.forEach {
                it.kotlinOptions {
                    freeCompilerArgs += "-Xnew-inference"
                }
            }
            binaries {
                sharedLib {
                }
                executable(listOf(DEBUG)) {
                }
                executable(listOf(RELEASE)) {
                }
            }
        }
    }
    if (compileWindows) {
        mingwX64("mingwX64") {
            compilations.forEach {
                it.kotlinOptions {
                    freeCompilerArgs += "-Xnew-inference"
                }
            }
            binaries {
                sharedLib {
                }
                executable(listOf(DEBUG)) {
                }
                executable(listOf(RELEASE)) {
                }
            }
        }
    }
    if (compileOSX) {
        macosX64("macosX64") {
            compilations.forEach {
                it.kotlinOptions {
                    freeCompilerArgs += "-Xnew-inference"
                }
            }
            binaries {
                sharedLib {
                }
                executable(listOf(DEBUG)) {
                }
                executable(listOf(RELEASE)) {
                }
            }
        }
    }
    if (compileJS) {
        js(IR) {
            moduleName = "simora"
            if (nodeJSMode) {
                nodejs {
                    binaries.executable()
                }
            } else {
                browser {
                    binaries.executable()
                }
            }
        }
    }
    sourceSets {
        val commonMain by getting {
            dependencies {
                implementation("org.jetbrains.kotlinx:kotlinx-datetime:0.4.0")
            }
        }
        val commonTest by getting {
            dependencies {
                implementation(kotlin("test-common"))
                implementation(kotlin("test-annotations-common"))
            }
        }
        if (compileJVM) {
            val jvmMain by getting {
                dependencies {
                    implementation("com.google.code.java-allocation-instrumenter:java-allocation-instrumenter:3.3.0")
                }
            }
            val jvmTest by getting {
                dependencies {
                    implementation(kotlin("test"))
                    implementation(kotlin("test-junit"))
                }
            }
        }
        if (compileLinux || compileOSX || compileWindows) {
            val desktopMain by creating {
                dependsOn(commonMain)
            }
            if (compileLinux) {
                val linuxX64Main by getting {
                    dependsOn(desktopMain)
                }
            }
            if (compileWindows) {
                val mingwX64Main by getting {
                    dependsOn(desktopMain)
                }
            }
            if (compileOSX) {
                val macosX64Main by getting {
                    dependsOn(desktopMain)
                }
            }
        }
        if (compileJS) {
            if (nodeJSMode) {
                val jsNodeMain by creating {
                    dependsOn(commonMain)
                }
                val jsMain by getting {
                    dependsOn(jsNodeMain)
                    dependencies {
                    }
                }
            } else {
                val jsBrowserMain by creating {
                    dependsOn(commonMain)
                }
                val jsMain by getting {
                    dependsOn(jsBrowserMain)
                    dependencies {
                    }
                }
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
if (compileJVM) {
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
}
if (compileJS) {
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
