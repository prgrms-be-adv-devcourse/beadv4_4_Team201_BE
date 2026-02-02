package app.giftify.funding.application;

import app.giftify.funding.adpater.outbound.jpa.Funding;
import app.giftify.funding.adpater.outbound.repository.FundingRepository;
import lombok.RequiredArgsConstructor;
import org.springframework.stereotype.Service;
import org.springframework.transaction.annotation.Transactional;

import java.util.Optional;

@Service
@RequiredArgsConstructor
public class FundingQueryUseCase {

    private final FundingRepository fundingRepository;

    @Transactional
    public boolean existsByWishlistItemId(Long wishlistItemId) {
        Optional<Funding> fundingOpt = fundingRepository.existsByWishlistItemId(wishlistItemId);
        return fundingOpt.isPresent();
    }
}
