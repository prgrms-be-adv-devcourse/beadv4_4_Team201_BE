plugins {
    // 플러그인은 루트에서 버전만 선언하고 apply false
    // 실제 적용은 각 모듈에서 선택적으로
    java
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
val parentModules = setOf("bc", "support", "bootstrap")

subprojects {
    // 부모 모듈이 아닌 경우에만 java 플러그인 적용
    if (name !in parentModules) {
        apply(plugin = "java")
        apply(plugin = "io.spring.dependency-management")

        // Java 21 설정
        configure<JavaPluginExtension> {
            toolchain {
                languageVersion.set(JavaLanguageVersion.of(21))
            }
            sourceCompatibility = JavaVersion.VERSION_21
            targetCompatibility = JavaVersion.VERSION_21
        }

        // Spring Boot BOM 전역 적용
        the<io.spring.gradle.dependencymanagement.dsl.DependencyManagementExtension>().apply {
            imports {
                mavenBom("org.springframework.boot:spring-boot-dependencies:3.2.2")
            }
        }

        // 테스트 설정
        tasks.withType<Test> {
            useJUnitPlatform()
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
    it.parent?.name == "bc" && it.name != "shared" && it.name != "bc"
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

            // Payment Adapter만 의존
            "implementation"(project(":bc:payment:adapter"))

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
            "runtimeOnly"("com.mysql:mysql-connector-j")
            "runtimeOnly"("com.h2database:h2")

            // Monitoring
            "runtimeOnly"("io.micrometer:micrometer-registry-prometheus")

            // Testing
            "testImplementation"("io.rest-assured:rest-assured")
        }
}
