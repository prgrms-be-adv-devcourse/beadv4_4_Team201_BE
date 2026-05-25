@ApplicationModule(
	displayName = "Friendship",
	allowedDependencies = { "member::*", "funding::*", "security", "support" }
)
package app.giftify.friendship;

import org.springframework.modulith.ApplicationModule;
