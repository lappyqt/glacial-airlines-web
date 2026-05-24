plugins {}

dependencies {
    implementation(project(":infrastructure"))
    implementation(project(":domain"))

    implementation(libs.bundles.spring.boot.application)
    testImplementation(libs.bundles.spring.boot.application.test)

    compileOnly(libs.project.lombok)
    annotationProcessor(libs.project.lombok)
}