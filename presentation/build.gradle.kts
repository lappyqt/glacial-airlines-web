plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":application"))

    implementation(libs.bundles.spring.boot.web)
    testImplementation(libs.bundles.spring.boot.web.test)
}