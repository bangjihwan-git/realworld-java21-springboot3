import org.springframework.boot.gradle.tasks.bundling.BootJar

dependencies {
    compileOnly(project(":module:core"))
    testCompileOnly(project(":module:core"))
    testImplementation(testFixtures(project(":module:core")))

    // Inject spring beans on runtime
    runtimeOnly(project(":module:persistence"))
    testRuntimeOnly(project(":module:persistence"))

    implementation(libs.spring.boot.starter.web)
    implementation(libs.spring.boot.starter.oauth2.resource.server)

    // Remove Jakarta Persistence API related warnings
    implementation(libs.jakarta.persistence.api)
}

tasks.getByName<BootJar>("bootJar") {
    enabled = true
}

tasks.getByName<Jar>("jar") {
    enabled = false
}
