@ApplicationModule(
	displayName = "Member",
	allowedDependencies = { "auth::*", "security", "support" }
)
package app.giftify.member;

import org.springframework.modulith.ApplicationModule;
