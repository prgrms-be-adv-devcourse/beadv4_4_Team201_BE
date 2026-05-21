package app.giftify.api.modulith;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.springframework.modulith.core.ApplicationModules;
import org.springframework.modulith.docs.Documenter;

import app.giftify.GiftifyApplication;

class ModularityTest {

	static final ApplicationModules MODULES = ApplicationModules.of(GiftifyApplication.class);

	@Test
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
