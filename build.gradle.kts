plugins {
    id("java")
}

repositories {
    // Add Google repository first
    google()
    // Use Maven Central with mirror
    mavenCentral()
    // Add Gradle plugin portal as fallback
    gradlePluginPortal()
}

dependencies {
    compileOnly("net.portswigger.burp.extensions:montoya-api:2025.8")
}

tasks.withType<JavaCompile>().configureEach {
    sourceCompatibility = "21"
    targetCompatibility = "21"
    options.encoding = "UTF-8"
}

tasks.named<Jar>("jar") {
    duplicatesStrategy = DuplicatesStrategy.EXCLUDE
    from(configurations.runtimeClasspath.get().filter { it.isDirectory })
    from(configurations.runtimeClasspath.get().filterNot { it.isDirectory }.map { zipTree(it) })
}