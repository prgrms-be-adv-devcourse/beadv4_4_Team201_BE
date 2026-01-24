package app.giftify.wishlist.application.service;

import static org.assertj.core.api.Assertions.*;
import static org.mockito.ArgumentMatchers.*;
import static org.mockito.BDDMockito.*;

import java.util.Optional;

import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.InjectMocks;
import org.mockito.Mock;
import org.mockito.junit.jupiter.MockitoExtension;

import app.giftify.shared.domain.event.EventPublisher;
import app.giftify.wishlist.application.port.in.UpdateWishlistSettingsUseCase;
import app.giftify.wishlist.application.port.out.WishlistRepositoryPort;
import app.giftify.wishlist.core.domain.Visibility;
import app.giftify.wishlist.core.domain.Wishlist;
import app.giftify.wishlist.core.domain.exception.WishlistNotFoundException;

@ExtendWith(MockitoExtension.class)
class WishlistServiceTest {

	@Mock
	private WishlistRepositoryPort wishlistRepositoryPort;

	@Mock
	private EventPublisher eventPublisher;

	@InjectMocks
	private WishlistService wishlistService;

	private static final Long MEMBER_ID = 1L;

	@Test
	@DisplayName("memberId로 위시리스트를 조회한다")
	void getOrCreateWishlistByMemberId() {
		// given
		Wishlist wishlist = Wishlist.builder()
			.memberId(MEMBER_ID)
			.visibility(Visibility.PUBLIC)
			.build();
		given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));

		// when
		Optional<Wishlist> result = Optional.of(wishlistService.getOrCreateWishlistByMemberId(MEMBER_ID));

		// then
		assertThat(result).isPresent();
		assertThat(result.get().getMemberId()).isEqualTo(MEMBER_ID);
	}

	@Test
	@DisplayName("위시리스트 설정을 변경한다")
	void updateSettings() {
		// given
		Visibility newVisibility = Visibility.PUBLIC;
		UpdateWishlistSettingsUseCase.UpdateSettingsCommand command =
			new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(MEMBER_ID, newVisibility);

		Wishlist wishlist = Wishlist.builder()
			.memberId(MEMBER_ID)
			.visibility(Visibility.PRIVATE)
			.build();
		given(wishlistRepositoryPort.findByMemberId(MEMBER_ID)).willReturn(Optional.of(wishlist));
		given(wishlistRepositoryPort.save(any(Wishlist.class))).willAnswer(invocation -> invocation.getArgument(0));

		// when
		Wishlist result = wishlistService.updateSettings(command);

		// then
		assertThat(result.getVisibility()).isEqualTo(newVisibility);
		verify(wishlistRepositoryPort).save(wishlist);
	}

	@Test
	@DisplayName("존재하지 않는 위시리스트의 설정을 변경하려 하면 예외가 발생한다")
	void updateSettingsFail() {
		// given
		Long notFoundMemberId = 999L;
		UpdateWishlistSettingsUseCase.UpdateSettingsCommand command =
			new UpdateWishlistSettingsUseCase.UpdateSettingsCommand(notFoundMemberId, Visibility.PUBLIC);

		given(wishlistRepositoryPort.findByMemberId(notFoundMemberId)).willReturn(Optional.empty());

		// when & then
		assertThatThrownBy(() -> wishlistService.updateSettings(command))
			.isInstanceOf(WishlistNotFoundException.class);
	}
}
