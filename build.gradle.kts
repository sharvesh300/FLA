plugins {
    id("java")
    id("application")
}

group = "org.example"
version = "1.0-SNAPSHOT"

repositories {
    mavenCentral()
}

dependencies {
    testImplementation(platform("org.junit:junit-bom:6.0.0"))
    testImplementation("org.junit.jupiter:junit-jupiter")
    testRuntimeOnly("org.junit.platform:junit-platform-launcher")
}

tasks.withType<JavaCompile>().configureEach {
    options.release = 17
    options.encoding = "UTF-8"
}

application {
    mainClass = "org.example.Main"
    applicationDefaultJvmArgs = listOf("-Dfile.encoding=UTF-8", "-Dstdout.encoding=UTF-8")
}

tasks.named<JavaExec>("run") {
    standardInput = System.`in`
}

tasks.test {
    useJUnitPlatform()
}
