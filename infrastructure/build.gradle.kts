plugins {}

dependencies {
    implementation(project(":domain"))

    implementation(libs.spring.boot.starter.data.jpa)
    runtimeOnly(libs.postgresql.jdbc.driver)

    compileOnly(libs.project.lombok)
    annotationProcessor(libs.project.lombok)
}