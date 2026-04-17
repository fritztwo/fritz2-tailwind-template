import com.google.devtools.ksp.gradle.KspTaskMetadata
import org.jetbrains.kotlin.gradle.plugin.KotlinDependencyHandler
import org.jetbrains.kotlin.gradle.tasks.KotlinCompilationTask
import kotlin.jvm.java

plugins {
    alias(libs.plugins.kotlin.multiplatform)
    alias(libs.plugins.google.ksp)
}

repositories {
    mavenCentral()
    maven("https://central.sonatype.com/repository/maven-snapshots/") // new repository here
}

//group = "my.fritz2.app"
//version = "0.0.1-SNAPSHOT"

kotlin {
    jvm()

    js(IR) {
        browser {
            commonWebpackConfig {
                cssSupport {
                    enabled = true
                }
            }
        }
        binaries.executable()
    }

    sourceSets {
        commonMain {
            dependencies {
                implementation(libs.fritz2.core)
                // implementation(libs.fritz2.headless) // optional
            }
            kotlin.srcDir("build/generated/ksp/metadata/commonMain/kotlin")
        }
        jvmMain {
            dependencies {
            }
        }
        jsMain {
            dependencies {
                implementation(npm(libs.tailwindcss.core))
                implementation(npm(libs.tailwindcss.postcss))
                implementation(npm(libs.postcss.core))
                implementation(npm(libs.postcss.loader))
            }
        }
    }
}

// KSP support for Lens generation
dependencies {
    kspCommonMainMetadata(libs.fritz2.lenses)
}

project.tasks.withType(KotlinCompilationTask::class.java).configureEach {
    if (name != "kspCommonMainKotlinMetadata") {
        dependsOn("kspCommonMainKotlinMetadata")
    }
}

// FIXME: Simple workaround to make version catalogs usable for npm dependencies too. Remove if kotlin plugin
//  supports this out of the box!
fun KotlinDependencyHandler.npm(dependency: Provider<MinimalExternalModuleDependency>): Dependency =
    dependency.map { dep ->
        val name = if (dep.group == "npm") dep.name else "@${dep.group}/${dep.name}"
        npm(name, dep.version!!)
    }.get()
