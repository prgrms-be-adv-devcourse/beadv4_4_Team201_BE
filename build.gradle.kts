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
// JaCoCo 커버리지 제외 패턴
// =============================================================================
// 테스트 정책에 따라 커버리지 측정에서 제외할 클래스 패턴
// - DTO, Entity, Config 등 테스트 불필요 클래스 제외
// - 핵심 비즈니스 로직(Service, Domain, Controller)만 측정
// =============================================================================
val jacocoExclusions = listOf(
    // Config 클래스
    "**/config/**",
    "**/*Config.class",
    "**/*Config$*.class",
    "**/*Properties.class",

    // DTO 클래스 (Request, Response, Command, Query, Result)
    "**/dto/**",
    "**/inbound/*Command.class",
    "**/inbound/*Query.class",
    "**/inbound/*Result.class",
    "**/pg/*Result.class",              // PG 응답 DTO (TossConfirmResult 등)

    // UseCase/Port 인터페이스
    "**/inbound/*UseCase.class",
    "**/outbound/*Port.class",
    "**/outbound/*Repository.class",    // Port 인터페이스 (Repository 패턴)
    "**/outbound/*Reader.class",        // Port 인터페이스 (Reader 패턴)
    "**/outbound/*Gateway.class",       // Port 인터페이스 (Gateway 패턴)
    "**/outbound/*Encryptor.class",     // Port 인터페이스 (Encryptor 패턴)
    "**/application/*UseCase.class",    // funding 모듈 UseCase (application 직하 위치)
    "**/port/in/*UseCase.class",        // member 모듈 UseCase (port/in 구조)
    "**/port/in/*UseCase$*.class",      // member 모듈 UseCase 내부 클래스
    "**/port/out/*Port.class",          // member 모듈 Port (port/out 구조)

    // JPA Entity
    "**/entity/**",
    "**/*Entity.class",
    "**/*JpaEntity.class",

    // 예외 및 에러코드
    "**/exception/**",
    "**/*Exception.class",
    "**/*ErrorCode.class",

    // 기타
    "**/*Mapper.class",
    "**/*DataInitializer.class"
)

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

        // Java 25 Toolchain
        // Note: Gradle 8.14.x는 Java 24까지 공식 지원.
        // Java 25 toolchain은 로컬/CI에 JDK가 설치되어 있으면 동작하지만,
        // 자동 다운로드(toolchain resolver)가 필요하면 Gradle 9.1.0+ 필요.
        // Gradle 9.x는 아직 preview 단계이므로 도입하지 않음.
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(25))
            }
        }

        tasks.withType<JavaCompile> {
            options.compilerArgs.add("-parameters")
        }

        // Spring Boot BOM 적용 (버전 관리)
        the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
            imports {
                mavenBom("org.springframework.boot:spring-boot-dependencies:${rootProject.libs.versions.springBoot.get()}")
            }
        }

        // Spring Boot BOM의 Testcontainers 버전을 version catalog 버전으로 오버라이드
        ext["lombok.version"] = rootProject.libs.versions.lombok.get()

        configurations.all {
            resolutionStrategy.eachDependency {
                if (requested.group == "org.testcontainers") {
                    useVersion(rootProject.libs.versions.testcontainers.get())
                }
            }
        }

        // JUnit Platform Launcher — Gradle 8.x 내장 launcher가 JUnit 6.x와 호환되지 않으므로 명시 추가
        dependencies {
            "testRuntimeOnly"("org.junit.platform:junit-platform-launcher")
        }

        // 테스트 설정
        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.withType<JacocoReport>())
        }

        // Jacoco 개별 리포트 설정
        tasks.withType<JacocoReport> {
            dependsOn(tasks.withType<Test>())

            classDirectories.setFrom(
                files(classDirectories.files.map {
                    fileTree(it) {
                        exclude(jacocoExclusions)
                    }
                })
            )

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
    classDirectories.setFrom(
        files(jacocoSubprojects.map { it.the<SourceSetContainer>()["main"].output }).asFileTree.matching {
            exclude(jacocoExclusions)
        }
    )
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
