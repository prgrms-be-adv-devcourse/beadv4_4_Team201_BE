package app.giftify.cart.application.inbound.usecase;

import app.giftify.cart.application.inbound.AddCartItemCommand;
import app.giftify.cart.core.domain.CartItemAddResult;

import java.util.List;

public interface AddCartItemUseCase {
    CartItemAddResult addItemToMyCart(Long memberId, AddCartItemCommand command);
    void addItemsToMyCart(Long memberId, List<AddCartItemCommand> commands);
}
