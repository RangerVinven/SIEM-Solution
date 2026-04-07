plugins {
	java
	id("org.springframework.boot") version "4.0.3"
	id("io.spring.dependency-management") version "1.1.7"
}

group = "siem.normalisation"
version = "0.0.1-SNAPSHOT"
description = "Log normalisation service"


configurations {
	compileOnly {
		extendsFrom(configurations.annotationProcessor.get())
	}
}

repositories {
	mavenCentral()
}

dependencies {
    implementation(project(":common:shared-models"))
    implementation(project(":common:shared-utils"))
    implementation(project(":common:shared-filters"))

	implementation("org.springframework.boot:spring-boot-starter-kafka")
	implementation("org.springframework.boot:spring-boot-starter-data-jpa")
	implementation("com.fasterxml.jackson.core:jackson-databind")
	runtimeOnly("org.postgresql:postgresql")
	compileOnly("org.projectlombok:lombok")
	annotationProcessor("org.projectlombok:lombok")
	testImplementation("org.springframework.boot:spring-boot-starter-kafka-test")
	testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<Test> {
	useJUnitPlatform()
}
