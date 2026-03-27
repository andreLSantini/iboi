plugins {
	kotlin("jvm") version "1.9.24"
	kotlin("plugin.spring") version "1.9.24"
	id("org.springframework.boot") version "3.3.5"
	id("io.spring.dependency-management") version "1.1.7"
	kotlin("plugin.jpa") version "1.9.24"
}

import org.jetbrains.kotlin.gradle.dsl.JvmTarget

group = "com.example"
version = "0.0.1-SNAPSHOT"

val detectedJavaVersion = JavaVersion.current().majorVersion.toInt()
val toolchainVersion = ((findProperty("javaToolchainVersion") as String?)?.toIntOrNull())
	?: if (detectedJavaVersion >= 17) detectedJavaVersion else 17

java {
	toolchain {
		languageVersion = JavaLanguageVersion.of(toolchainVersion)
	}
}

repositories {
	mavenCentral()
}

dependencies {
	// Web + Security
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("org.springframework.boot:spring-boot-starter-security")
	implementation("org.springframework.boot:spring-boot-starter-oauth2-resource-server")

	// Kotlin + Jackson
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")

	// Swagger / OpenAPI
	implementation("org.springdoc:springdoc-openapi-starter-webmvc-ui:2.6.0")

	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("org.flywaydb:flyway-core")

	runtimeOnly("com.h2database:h2")
	runtimeOnly("org.postgresql:postgresql")

	implementation("org.jetbrains.kotlin:kotlin-reflect")

	implementation("io.jsonwebtoken:jjwt-api:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-impl:0.11.5")
	runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.11.5")


	// Tests
	testImplementation("org.springframework.boot:spring-boot-starter-test")
	testImplementation("org.springframework.security:spring-security-test")
}

kotlin {
	jvmToolchain(toolchainVersion)
	compilerOptions {
		jvmTarget.set(JvmTarget.JVM_17)
		freeCompilerArgs.addAll(
				"-Xjsr305=strict",
				"-Xannotation-default-target=param-property"
		)
	}
}

tasks.withType<JavaCompile> {
	options.release.set(17)
}

tasks.withType<Test> {
	useJUnitPlatform()
}
