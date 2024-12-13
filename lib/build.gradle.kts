import org.jreleaser.model.Active

plugins {
    // Apply the org.jetbrains.kotlin.jvm Plugin to add support for Kotlin.
    alias(libs.plugins.kotlin.jvm)

    // Apply the java-library plugin for API and implementation separation.
    `java-library`

    groovy

    alias(libs.plugins.dokka)

    alias(libs.plugins.license)

    id("maven-publish")
    id("org.jreleaser") version "1.15.0"
    id("signing")

}

repositories {
    // Use Maven Central for resolving dependencies.
    mavenCentral()
}

dependencies {
    // Use the Kotlin JUnit 5 integration.
    testImplementation("org.jetbrains.kotlin:kotlin-test-junit5")

    // Use the JUnit 5 integration.
    testImplementation(libs.junit.jupiter.engine)
    // Use Spock
    testImplementation("org.spockframework:spock-core:2.4-M4-groovy-4.0")
    testImplementation("org.apache.groovy:groovy-all:4.0.24")

    testRuntimeOnly("org.junit.platform:junit-platform-launcher")

}

// Apply a specific Java toolchain to ease working on different environments.
java {
    toolchain {
        languageVersion = JavaLanguageVersion.of(11)
    }
    withJavadocJar()
    withSourcesJar()
}

val javadocJar = tasks.named<Jar>("javadocJar") {
    from(tasks.named("dokkaJavadoc"))
}

tasks.jar {
    enabled = true
    // Remove `plain` postfix from jar file name
    archiveClassifier.set("")
}

license {
    include("**/*.kt")
    include("**/*.groovy")
    mapping(
        mapOf(
            "kt" to "SLASHSTAR_STYLE",
            "groovy" to "SLASHSTAR_STYLE"
        )
    )
}

publishing {
    publications {
        create<MavenPublication>("Maven") {
            from(components["java"])
            groupId = "io.github.j0s3f.kotlin"
            artifactId = "rangesets"
            description = "Kotlin Range Sets"
        }
        withType<MavenPublication> {
            pom {
                packaging = "jar"
                name.set("rangesets")
                description.set("Kotlin Range Sets")
                url.set("https://github.com/J0s3f/kotlin-range-sets/")
                inceptionYear.set("2024")
                licenses {
                    license {
                        name.set("MIT license")
                        url.set("https://opensource.org/licenses/MIT")
                    }
                }
                developers {
                    developer {
                        id.set("j0s3f")
                        name.set("Josef Schneider")
                        email.set("git@netpage.dk")
                    }
                }
                scm {
                    connection.set("scm:git:git@github.com:J0s3f/kotlin-range-sets.git")
                    developerConnection.set("scm:git:ssh:git@github.com:J0s3f/kotlin-range-sets.git.git")
                    url.set("https://github.com/J0s3f/kotlin-range-sets/")
                }
            }
        }
    }
    repositories {
        maven {
            url = layout.buildDirectory.dir("staging-deploy").get().asFile.toURI()
        }
    }
}

jreleaser {
    project {
        copyright.set("Josef Schneider")
    }
    gitRootSearch.set(true)
    signing {
        active.set(Active.ALWAYS)
        armored.set(true)
    }
    deploy {
        maven {
            mavenCentral {
                create("sonatype") {
                    active.set(Active.ALWAYS)
                    url.set("https://central.sonatype.com/api/v1/publisher")
                    stagingRepositories.add(layout.buildDirectory.dir("staging-deploy").get().asFile.path)
                }
            }
        }
    }
}

tasks.named<Test>("test") {
    // Use JUnit Platform for unit tests.
    useJUnitPlatform()
    testLogging {
        events("passed", "skipped", "failed")
    }
}
