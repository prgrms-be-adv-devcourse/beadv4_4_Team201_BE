package app.giftify.payment;

import com.tngtech.archunit.core.domain.JavaClasses;
import com.tngtech.archunit.core.importer.ClassFileImporter;
import com.tngtech.archunit.core.importer.ImportOption;
import com.tngtech.archunit.lang.ArchRule;
import com.tngtech.archunit.library.Architectures;
import org.junit.jupiter.api.BeforeAll;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Nested;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.condition.EnabledIf;

import static com.tngtech.archunit.lang.syntax.ArchRuleDefinition.noClasses;

/**
 * Payment 모듈의 헥사고날 아키텍처 의존성 규칙을 검증합니다.
 *
 * <p>의존성 방향: adapter → application → domain
 * <ul>
 *   <li>Domain: 순수 비즈니스 로직, 외부 의존성 없음</li>
 *   <li>Application: UseCase, Port 정의</li>
 *   <li>Adapter: 외부 세계와의 연결 (Web, JPA, External API)</li>
 * </ul>
 */
class HexagonalArchitectureTest {

    private static final String PAYMENT_PACKAGE = "app.giftify.payment";

    private static JavaClasses classes;

    @BeforeAll
    static void setUp() {
        classes = new ClassFileImporter()
                .withImportOption(ImportOption.Predefined.DO_NOT_INCLUDE_TESTS)
                .importPackages(PAYMENT_PACKAGE);
    }

    @Nested
    @DisplayName("Given 헥사고날 아키텍처가 적용된 Payment 모듈에서")
    class Given_헥사고날_아키텍처가_적용된_Payment_모듈에서 {

        @Nested
        @DisplayName("When 계층 간 의존성을 검사하면")
        class When_계층_간_의존성을_검사하면 {

            @Test
            @DisplayName("Then adapter → application → domain 방향으로만 의존해야 한다")
            void Then_adapter에서_application으로_application에서_domain으로만_의존해야_한다() {
                Architectures.LayeredArchitecture layeredArchitecture = Architectures.layeredArchitecture()
                        .consideringOnlyDependenciesInLayers()
                        // 계층 정의 (optionalLayer: 비어있어도 OK)
                        .optionalLayer("Adapter").definedBy("..adapter..")
                        .optionalLayer("Application").definedBy("..application..")
                        .optionalLayer("Domain").definedBy("..domain..")
                        // 의존성 규칙
                        .whereLayer("Adapter").mayOnlyAccessLayers("Application", "Domain")
                        .whereLayer("Application").mayOnlyAccessLayers("Domain")
                        .whereLayer("Domain").mayNotAccessAnyLayer();

                layeredArchitecture.check(classes);
            }
        }
    }

    @Nested
    @DisplayName("Given Domain 계층에서")
    class Given_Domain_계층에서 {

        @Nested
        @DisplayName("When 외부 프레임워크 의존성을 검사하면")
        class When_외부_프레임워크_의존성을_검사하면 {

            @Test
            @DisplayName("Then Spring 프레임워크에 의존하지 않아야 한다")
            void Then_Spring_프레임워크에_의존하지_않아야_한다() {
                ArchRule rule = noClasses()
                        .that().resideInAPackage("..domain..")
                        .should().dependOnClassesThat()
                        .resideInAnyPackage("org.springframework..");

                rule.check(classes);
            }

            @Test
            @DisplayName("Then JPA/Persistence에 의존하지 않아야 한다")
            void Then_JPA_Persistence에_의존하지_않아야_한다() {
                ArchRule rule = noClasses()
                        .that().resideInAPackage("..domain..")
                        .should().dependOnClassesThat()
                        .resideInAnyPackage(
                                "jakarta.persistence..",
                                "javax.persistence..",
                                "org.hibernate.."
                        );

                rule.check(classes);
            }
        }
    }

    @Nested
    @DisplayName("Given Application 계층에서")
    class Given_Application_계층에서 {

        @Nested
        @DisplayName("When Adapter 의존성을 검사하면")
        class When_Adapter_의존성을_검사하면 {

            @Test
            @DisplayName("Then Adapter 계층에 의존하지 않아야 한다")
            @EnabledIf("hasApplicationClasses")
            void Then_Adapter_계층에_의존하지_않아야_한다() {
                ArchRule rule = noClasses()
                        .that().resideInAPackage("..application..")
                        .should().dependOnClassesThat()
                        .resideInAPackage("..adapter..");

                rule.check(classes);
            }

            boolean hasApplicationClasses() {
                return classes.stream()
                        .anyMatch(c -> c.getPackageName().contains(".application"));
            }
        }
    }
}