// =============================================================================
// Giftify Backend - Root Build Configuration
// =============================================================================
// 원칙: 각 모듈이 자신의 의존성을 관리, 루트는 최소한의 공통 설정만 제공
// =============================================================================

plugins {
    java
    jacoco
    alias(libs.plugins.spring.boot) apply false
    alias(libs.plugins.spring.dependency.management) apply false
}

// =============================================================================
// 전체 프로젝트 공통 설정
// =============================================================================
allprojects {
    group = "app.giftify"
    version = "1.0.0-SNAPSHOT"

    repositories {
        mavenCentral()
    }
}

// =============================================================================
// Java 서브프로젝트 공통 설정
// =============================================================================
val containerModules = setOf("bc", "support", "bootstrap", "money")

subprojects {
    if (name !in containerModules) {
        apply(plugin = "java")
        apply(plugin = "jacoco")
        apply(plugin = "io.spring.dependency-management")

        // Java 21 Toolchain
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
        }

        // Spring Boot BOM 적용 (버전 관리)
        the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
            imports {
                mavenBom("org.springframework.boot:spring-boot-dependencies:${rootProject.libs.versions.springBoot.get()}")
            }
        }

        // 테스트 설정
        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.withType<JacocoReport>())
        }

        // Jacoco 개별 리포트 설정
        tasks.withType<JacocoReport> {
            dependsOn(tasks.withType<Test>())
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }
    }
}

// =============================================================================
// Jacoco 집계 리포트 설정
// =============================================================================
tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated Jacoco coverage report for all subprojects"

    val jacocoSubprojects = subprojects.filter { it.name !in containerModules }

    dependsOn(jacocoSubprojects.map { it.tasks.matching { task -> task.name == "test" } })

    additionalSourceDirs.setFrom(jacocoSubprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    sourceDirectories.setFrom(jacocoSubprojects.map { it.the<SourceSetContainer>()["main"].allSource.srcDirs })
    classDirectories.setFrom(jacocoSubprojects.map { it.the<SourceSetContainer>()["main"].output })
    executionData.setFrom(
        jacocoSubprojects.flatMap { subproject ->
            subproject.tasks.withType<Test>().map { it.extensions.getByType<JacocoTaskExtension>().destinationFile }
        }
    )

    reports {
        xml.required.set(true)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated/jacocoTestReport.xml"))
        html.required.set(true)
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated"))
    }
}
