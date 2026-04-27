plugins {}

dependencies {
    implementation(project(":infrastructure"))

    implementation(libs.bundles.spring.boot.application)
    testImplementation(libs.bundles.spring.boot.application.test)
}