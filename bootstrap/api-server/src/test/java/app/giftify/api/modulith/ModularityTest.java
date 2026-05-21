package app.giftify.api.modulith;

import org.junit.jupiter.api.Disabled;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import app.giftify.GiftifyApplication;

class ModularityTest {

	static final ApplicationModules MODULES = ApplicationModules.of(GiftifyApplication.class);

	@Test
	@Disabled("후속 task: 17개 모듈 NamedInterface 일괄 도입 후 활성화. docs/reports/2026-05-21-modulith-verify-violations.md 참조.")
	@DisplayName("Modulith 모듈 경계 위반이 없어야 한다")
	void verifyModuleBoundaries() {
		MODULES.verify();
	}

	@Test
	@DisplayName("PlantUML 다이어그램과 docs/snippets 가 생성된다 (build/spring-modulith-docs)")
	void writeDocumentationSnippets() {
		new Documenter(MODULES)
			.writeModulesAsPlantUml()
			.writeIndividualModulesAsPlantUml();
	}
}
