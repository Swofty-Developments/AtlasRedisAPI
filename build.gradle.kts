plugins {
    java
}

group = "net.swofty"
version = project.findProperty("version") ?: "0.0.0-SNAPSHOT" // handled by semantic-release

java {
    toolchain {
        languageVersion.set(JavaLanguageVersion.of(25))
    }
}

repositories {
    mavenCentral()
}

dependencies {
    compileOnly("org.projectlombok:lombok:1.18.42")
    annotationProcessor("org.projectlombok:lombok:1.18.42")

    implementation("redis.clients:jedis:7.2.0")
}

tasks.withType<JavaCompile>().configureEach {
    options.encoding = "UTF-8"
}

tasks.register("printVersion") {
    doLast {
        println(project.version)
    }
}

tasks.jar {
    archiveVersion.set(project.version.toString())
    archiveClassifier.set("")
}
