import org.jetbrains.kotlin.gradle.tasks.KotlinCompile

plugins {
	id("org.springframework.boot") version "2.2.2.RELEASE"
	id("io.spring.dependency-management") version "1.0.8.RELEASE"
	kotlin("jvm") version "1.3.61"
	kotlin("plugin.spring") version "1.3.61"
}

group = "com.utsman.covid19"
version = "0.0.1-SNAPSHOT"
java.sourceCompatibility = JavaVersion.VERSION_1_8

repositories {
	mavenCentral()
}

dependencies {
	implementation("org.springframework.boot:spring-boot-starter-web")
	implementation("com.fasterxml.jackson.module:jackson-module-kotlin")
	implementation("org.jetbrains.kotlin:kotlin-reflect")
	implementation("org.jetbrains.kotlin:kotlin-stdlib-jdk8")
	testImplementation("org.springframework.boot:spring-boot-starter-test") {
		exclude(group = "org.junit.vintage", module = "junit-vintage-engine")
	}
	implementation("com.github.doyaaaaaken:kotlin-csv-jvm:0.7.3")
	implementation("org.json:json:20180813")
	implementation("com.fasterxml.jackson.core:jackson-databind:2.9.7")
	implementation("com.fasterxml.jackson.dataformat:jackson-dataformat-xml:2.9.7")
	implementation("com.rometools:rome-modules:1.8.0")
	implementation("org.jsoup:jsoup:1.13.1")
	implementation("com.vladsch.flexmark:flexmark-all:0.60.2")
}

tasks.withType<Test> {
	useJUnitPlatform()
}

tasks.withType<KotlinCompile> {
	kotlinOptions {
		freeCompilerArgs = listOf("-Xjsr305=strict")
		jvmTarget = "1.8"
	}
}
