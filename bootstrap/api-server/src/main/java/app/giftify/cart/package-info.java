@ApplicationModule(
	displayName = "Cart",
	allowedDependencies = { "funding::*", "member::*", "product::*", "wishlist::*", "order::*", "support", "security" }
)
package app.giftify.cart;

import org.springframework.modulith.ApplicationModule;
