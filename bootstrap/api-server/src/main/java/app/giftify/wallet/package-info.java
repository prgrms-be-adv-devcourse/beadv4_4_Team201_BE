@ApplicationModule(
	displayName = "Wallet",
	allowedDependencies = { "settlement::*", "payment::*", "support", "security" }
)
package app.giftify.wallet;

import org.springframework.modulith.ApplicationModule;
