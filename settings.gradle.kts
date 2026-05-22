plugins {
    id("org.gradle.toolchains.foojay-resolver-convention") version "1.0.0"
}

enableFeaturePreview("TYPESAFE_PROJECT_ACCESSORS")

rootProject.name = "giftify-be"

// =============================================================================
// BC Modules (Business Capability)
// =============================================================================
include(
    "bc",
    "bc:shared",

    "bc:product",
    "bc:wallet",
    "bc:order",
    "bc:payment",
)




// =============================================================================
// Support Modules (공통 인프라 지원)
// =============================================================================
include(
    "support",
    "support:common",
    "support:logging",
    "support:security",
    "support:web",
    "support:jpa",
)

// =============================================================================
// Bootstrap (애플리케이션 진입점)
// =============================================================================
include(
    "bootstrap",
    "bootstrap:api-server",
)
