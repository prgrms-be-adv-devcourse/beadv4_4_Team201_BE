plugins {
    kotlin("jvm")
}

// Container module - disable build tasks
tasks.withType<Jar> { enabled = false }
tasks.withType<org.springframework.boot.gradle.tasks.bundling.BootJar> { enabled = false }
tasks.withType<Test> { enabled = false }
