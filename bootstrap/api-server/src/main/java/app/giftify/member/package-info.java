@ApplicationModule(
	displayName = "Member",
	allowedDependencies = { "auth::*", "shared", "security", "support" }
)
package app.giftify.member;

import org.springframework.modulith.ApplicationModule;
