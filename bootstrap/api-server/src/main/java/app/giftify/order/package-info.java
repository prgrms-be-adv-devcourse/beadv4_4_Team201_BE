@ApplicationModule(
	displayName = "Order",
	allowedDependencies = { "payment::*", "funding::*", "product::*", "support", "security" }
)
package app.giftify.order;

import org.springframework.modulith.ApplicationModule;
