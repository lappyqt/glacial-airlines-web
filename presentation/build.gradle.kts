import org.springframework.boot.gradle.tasks.run.BootRun

plugins {
    id("org.springframework.boot")
}

dependencies {
    implementation(project(":application"))
    implementation(project(":infrastructure"))
    implementation(project(":domain"))

    implementation(libs.spring.boot.docker.compose)
    developmentOnly(libs.spring.boot.dev.tools)

    implementation(libs.bundles.spring.boot.web)
    testImplementation(libs.bundles.spring.boot.web.test)

    compileOnly(libs.project.lombok)
    annotationProcessor(libs.project.lombok)
}

tasks {
    named<BootRun>("bootRun") {
        dependsOn(buildUILibrary)
    }

    processResources {
        dependsOn(buildUILibrary)
    }
}

tasks.clean {
    delete("${projectDir}/src/main/resources/static/dist")
}

val buildUILibrary = tasks.register<Exec>("buildUILibrary") {
    description = "Builds the UI Library"
    group = "build"

    workingDir("./webapp")
    commandLine("bun", "run", "build.ts")

    inputs.dir("webapp")
    outputs.dir("${projectDir}/src/main/resources/static")

    doLast {
        println("Build finished successfully...")
    }
}