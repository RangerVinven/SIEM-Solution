plugins {
    id("siem.java-conventions")
    id("io.spring.dependency-management") version "1.1.7"
}

dependencyManagement {
    imports {
        mavenBom("org.springframework.boot:spring-boot-dependencies:4.0.3")
    }
}

dependencies {
    implementation(project(":common:shared-utils"))
    implementation("org.springframework.boot:spring-boot-starter-security")
    implementation("jakarta.servlet:jakarta.servlet-api")

    compileOnly("org.projectlombok:lombok")
    annotationProcessor("org.projectlombok:lombok")
}
