plugins {
    id("siem.java-conventions")
}

dependencies {
    implementation("io.github.cdimascio:dotenv-java:3.0.0")
    implementation("io.jsonwebtoken:jjwt-api:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-impl:0.13.0")
    runtimeOnly("io.jsonwebtoken:jjwt-jackson:0.13.0")
    
    implementation("org.springframework.boot:spring-boot-starter:3.4.3")
}
