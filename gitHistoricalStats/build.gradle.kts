import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.7.10"
    id("application")
}

group = "nl.nielsvanhove"
version = "1.0.2"

repositories {
    mavenCentral()
}

dependencies {
    implementation(libs.argParser)
    implementation(libs.serialization)
    implementation(libs.letsplot.jvm)
    implementation(libs.letsplot.export)

    testImplementation(kotlin("test-junit5"))
    testImplementation(libs.junit.api)
    testRuntimeOnly(libs.junit.engine)
}

tasks.test {
    useJUnitPlatform()
    testLogging {
        setEvents(listOf("PASSED", "SKIPPED", "FAILED", "STANDARD_OUT", "STANDARD_ERROR"))
    }
}

tasks.withType<KotlinCompile> {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("nl.nielsvanhove.githistoricalstats.Main")
}
