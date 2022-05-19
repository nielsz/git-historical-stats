import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
    kotlin("jvm") version "1.6.20"
    application
}

group = "nl.nielsvanhove"
version = "1.0.1"

repositories {
    mavenCentral()
}

dependencies {
    implementation("com.xenomachina:kotlin-argparser:2.0.7")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:1.3.3")
    implementation("org.jetbrains.lets-plot:lets-plot-kotlin-jvm:3.2.0")
    implementation("org.jetbrains.lets-plot:lets-plot-image-export:2.3.0")


    testImplementation(kotlin("test-junit5"))
    testImplementation("org.junit.jupiter:junit-jupiter-api:5.8.2")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:5.8.2")
}

tasks.test {
    useJUnitPlatform()
}

tasks.withType<KotlinCompile>() {
    kotlinOptions.jvmTarget = "11"
}

application {
    mainClass.set("nl.nielsvanhove.githistoricalstats.Main")
}