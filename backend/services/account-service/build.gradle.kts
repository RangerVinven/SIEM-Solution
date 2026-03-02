plugins {
    id("dev.danielmcpherson.java-conventions")
}

dependencies {
    implementation(project(":common:shared-models"))
    implementation(libs.spring.boot.web)
}
