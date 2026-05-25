@ApplicationModule(
	displayName = "Notification",
	allowedDependencies = { "product::*", "order::*", "payment::*", "funding::*", "support", "security" }
)
package app.giftify.notification;

import org.springframework.modulith.ApplicationModule;
