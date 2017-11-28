import org.gradle.kotlin.dsl.repositories

buildscript {
    repositories {
        jcenter()
        mavenCentral()
    }
    dependencies {
        classpath("org.junit.platform:junit-platform-gradle-plugin:1.0.2")
    }
}

plugins {
    application
    kotlin("jvm") version "1.1.60"

    id("com.github.johnrengelman.shadow") version "2.0.1"
}

apply {
    plugin("org.junit.platform.gradle.plugin")
}

application {
    mainClassName = "com.redhat.mqe.MainKt"
}

repositories {
    jcenter()
}

dependencies {
    compile(group = "org.apache.commons", name = "commons-compress", version = "1.15")
    compile(kotlin("stdlib-jre8"))

    testCompile(kotlin("test"))
    testCompile("com.google.truth:truth:0.36")
    val junitPlatformVersion = "1.0.1"
    val junitJupiterVersion = "5.0.1"
    val junit4Version = "4.12"
    val junitVintageVersion = "4.12.2"
    val log4jVersion = "2.9.1"

    testImplementation("org.junit.jupiter:junit-jupiter-api:$junitJupiterVersion")
    testRuntimeOnly("org.junit.jupiter:junit-jupiter-engine:$junitJupiterVersion")

    // If you also want to support JUnit 3 and JUnit 4 tests
    testCompile("junit:junit:$junit4Version")
    testRuntime("org.junit.vintage:junit-vintage-engine:$junitVintageVersion")

    // To use Log4J"s LogManager
    testRuntimeOnly("org.apache.logging.log4j:log4j-core:$log4jVersion")
    testRuntimeOnly("org.apache.logging.log4j:log4j-jul:$log4jVersion")
}
