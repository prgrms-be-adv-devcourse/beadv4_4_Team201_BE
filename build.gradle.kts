plugins {
    // 플러그인은 루트에서 버전만 선언하고 apply false
    // 실제 적용은 각 모듈에서 선택적으로
    java
    jacoco
    alias(libs.plugins.kotlin.jvm) apply false
    alias(libs.plugins.kotlin.spring) apply false
    alias(libs.plugins.kotlin.jpa) apply false
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
// 모든 서브프로젝트 공통 설정 (기본 언어: Java)
// =============================================================================
// 부모 모듈 목록
val parentModules = setOf("bc", "support", "bootstrap", "money")

subprojects {
    // 부모 모듈이 아닌 경우에만 java 플러그인 적용
    if (name !in parentModules) {
        apply(plugin = "java")
        apply(plugin = "jacoco")
        apply(plugin = "io.spring.dependency-management")

        // Java 21 설정
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        // Spring Boot BOM 전역 적용 (Version Catalog 참조)
        the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
            imports {
                mavenBom("org.springframework.boot:spring-boot-dependencies:${rootProject.libs.versions.springBoot.get()}")
            }
        }

        // 테스트 설정
        tasks.withType<Test> {
            useJUnitPlatform()
            finalizedBy(tasks.named("jacocoTestReport"))
        }

        // JaCoCo 리포트 설정
        tasks.withType<JacocoReport> {
            reports {
                xml.required.set(true)
                html.required.set(true)
            }
        }

        // 공통 테스트 의존성
        dependencies {
            "testImplementation"("org.springframework.boot:spring-boot-starter-test")
            "testImplementation"("org.junit.jupiter:junit-jupiter")
        }
    }
}

// =============================================================================
// BC 모듈 전용 설정 (Business Capability)
// =============================================================================
// bc의 하위 모듈 중 shared가 아닌 모듈은 자동으로 bc:shared 의존
configure(subprojects.filter {
    it.path.startsWith(":bc") && it.name != "shared" && it.name !in parentModules
}) {
    dependencies {
        "implementation"(project(":bc:shared"))
    }
}

// =============================================================================
// Support 모듈 전용 설정 (공통 인프라 지원)
// =============================================================================
// support의 하위 모듈들만 필터링 (support 부모는 제외)
configure(subprojects.filter {
    it.parent?.name == "support" && it.name != "support"
}) {
        apply(plugin = "java-library")

        dependencies {
            "implementation"("org.springframework.boot:spring-boot-starter")
        }

        // Support 모듈은 라이브러리 (실행 가능한 Jar가 아님)
        tasks.named<Jar>("jar") {
            enabled = true
        }

        tasks.whenTaskAdded {
            if (name == "bootJar") {
                enabled = false
            }
        }
    }

// =============================================================================
// Bootstrap 모듈 전용 설정 (애플리케이션 진입점)
// =============================================================================
// bootstrap의 하위 모듈들만 필터링 (bootstrap 부모는 제외)
configure(subprojects.filter {
    it.parent?.name == "bootstrap" && it.name != "bootstrap"
}) {
        apply(plugin = "org.springframework.boot")

        dependencies {
            // 모든 BC 모듈 조립
            "implementation"(project(":bc:shared"))
            "implementation"(project(":bc:member"))
            "implementation"(project(":bc:auth"))
            "implementation"(project(":bc:funding"))

            //"implementation"(project(":bc:money:adapter"))

            // 모든 Support 모듈 (경로 수정!)
            "implementation"(project(":support:common"))
            "implementation"(project(":support:logging"))
            "implementation"(project(":support:security"))
            "implementation"(project(":support:web"))

            // Spring Boot Starters
            "implementation"("org.springframework.boot:spring-boot-starter-web")
            "implementation"("org.springframework.boot:spring-boot-starter-data-jpa")
            "implementation"("org.springframework.boot:spring-boot-starter-validation")
            "implementation"("org.springframework.boot:spring-boot-starter-actuator")

            // Database Drivers
            "runtimeOnly"("com.h2database:h2")
            "runtimeOnly"("org.postgresql:postgresql")

            // Monitoring
            "runtimeOnly"("io.micrometer:micrometer-registry-prometheus")

            // Testing
            "testImplementation"("io.rest-assured:rest-assured")
        }
}

// =============================================================================
// JaCoCo Aggregated Report (CI 커버리지 리포팅용)
// =============================================================================
jacoco {
    toolVersion = "0.8.11"
}

tasks.register<JacocoReport>("jacocoAggregatedReport") {
    group = "verification"
    description = "Generates aggregated JaCoCo report for all subprojects"

    val jacocoSubprojects = subprojects.filter {
        it.name !in parentModules && it.plugins.hasPlugin("jacoco")
    }

    dependsOn(jacocoSubprojects.map { it.tasks.named("test") })

    additionalSourceDirs.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    sourceDirectories.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].allSource.srcDirs }
    )
    classDirectories.setFrom(
        jacocoSubprojects.flatMap { it.the<SourceSetContainer>()["main"].output }
    )

    jacocoSubprojects.forEach { subproject ->
        executionData.from(
            fileTree(subproject.layout.buildDirectory) {
                include("jacoco/test.exec")
            }
        )
    }

    reports {
        xml.required.set(true)
        html.required.set(true)
        csv.required.set(false)
        xml.outputLocation.set(layout.buildDirectory.file("reports/jacoco/aggregated/jacocoTestReport.xml"))
        html.outputLocation.set(layout.buildDirectory.dir("reports/jacoco/aggregated/html"))
    }
}
