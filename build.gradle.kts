// SPDX-FileCopyrightText: 2025 Deutsche Telekom AG and others
//
// SPDX-License-Identifier: Apache-2.0

plugins {
    kotlin("jvm") version "2.2.21"
    kotlin("plugin.serialization") version "2.2.21"
    kotlin("plugin.spring") version "2.2.21"
    id("org.springframework.boot") version "3.4.0"
    id("io.spring.dependency-management") version "1.1.7"
    id("org.graalvm.buildtools.native") version "0.11.4"
}

group = "fr.bloctave.arena"

java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(24)
    }
}

kotlin {
    compilerOptions {
        freeCompilerArgs.addAll("-Xjsr305=strict", "-Xcontext-receivers")
    }
}

configurations.all {
    exclude(group = "commons-logging", module = "commons-logging")
}


tasks.withType<JavaExec> {
    jvmArgs("--enable-native-access=ALL-UNNAMED")
}


tasks.withType<Test> {
    useJUnitPlatform()
    jvmArgs("--enable-native-access=ALL-UNNAMED")

}

dependencies {
    val arcVersion = "0.206.0"
    val langchain4jVersion = "1.9.1"


    // Kotlin/Kotlinx
    val coroutinesVersion = "1.10.2"
    val reflectionVersion = "2.2.21"
    val serializationVersion = "1.9.0"

    // Arc
    implementation("org.eclipse.lmos:arc-spring-boot-starter:$arcVersion")
    implementation("org.eclipse.lmos:arc-assistants:$arcVersion")
    implementation("org.eclipse.lmos:arc-readers:$arcVersion")
    implementation("org.eclipse.lmos:arc-api:$arcVersion")
    implementation("org.eclipse.lmos:arc-graphql-spring-boot-starter:$arcVersion")
    implementation("org.eclipse.lmos:arc-view-spring-boot-starter:$arcVersion")
    implementation("org.eclipse.lmos:arc-langchain4j-client:${arcVersion}")

    // Tracing
    implementation(platform("io.micrometer:micrometer-tracing-bom:1.6.0"))
    implementation("io.micrometer:micrometer-tracing")
    implementation("io.micrometer:micrometer-tracing-bridge-otel")


    implementation(platform("io.opentelemetry:opentelemetry-bom:1.56.0"))
    implementation("io.opentelemetry:opentelemetry-api")
    implementation("io.opentelemetry:opentelemetry-context")
    implementation("io.opentelemetry:opentelemetry-exporter-otlp")
    implementation("io.opentelemetry:opentelemetry-extension-kotlin")


    // Azure
    implementation("com.azure:azure-identity:1.18.1")

    // Spring Boot
    implementation("org.springframework.boot:spring-boot-starter-actuator:3.4.0")
    implementation("org.springframework.boot:spring-boot-starter-web:3.4.0")

    // Langchain4j
    implementation("dev.langchain4j:langchain4j-bedrock:$langchain4jVersion")
    implementation("dev.langchain4j:langchain4j-google-ai-gemini:$langchain4jVersion")
    implementation("dev.langchain4j:langchain4j-ollama:$langchain4jVersion")
    implementation("dev.langchain4j:langchain4j-open-ai:$langchain4jVersion")

    // Metrics
    implementation("io.micrometer:micrometer-registry-prometheus")


    implementation("org.jetbrains.kotlin:kotlin-reflect:$reflectionVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-serialization-json:$serializationVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-core:$coroutinesVersion")
    implementation("org.jetbrains.kotlinx:kotlinx-coroutines-reactor:$coroutinesVersion")

    implementation("com.fasterxml.jackson.module:jackson-module-kotlin")



    // Test
    testImplementation("org.testcontainers:mongodb:1.21.3")
    testImplementation("org.springframework.boot:spring-boot-testcontainers:3.4.0")
    testImplementation("org.springframework.boot:spring-boot-starter-test:3.4.0")
    testImplementation("io.opentelemetry:opentelemetry-sdk-testing")

}

val otelVersion = "1.39.0" // Compatible OTel version

dependencyManagement {
    imports {
        // The Spring Boot BOM is applied by the plugin
        // We add the OpenTelemetry BOM to align all OTel transitive deps
        mavenBom("io.opentelemetry:opentelemetry-bom:$otelVersion")
        mavenBom("io.opentelemetry.instrumentation:opentelemetry-instrumentation-bom:2.22.0")
    }
}

repositories {
    mavenLocal()
    mavenCentral()
    maven(url = "https://oss.sonatype.org/content/repositories/snapshots/")
}

springBoot {
    mainClass.set("fr.bloctave.arena.ArcApplicationKt")
}