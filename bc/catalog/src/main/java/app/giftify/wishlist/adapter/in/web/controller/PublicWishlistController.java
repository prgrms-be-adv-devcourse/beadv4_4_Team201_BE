package app.giftify.wishlist.adapter.in.web.controller;

import app.giftify.product.application.support.ProductSupport;
import app.giftify.replica.member.Member;
import app.giftify.replica.member.MemberRepository;
import app.giftify.shared.api.response.RsData;
import app.giftify.wishlist.adapter.in.web.responseDto.MemberWishlistSummaryResponse;
import app.giftify.wishlist.application.port.in.GetPublicWishlistUseCase;
import app.giftify.wishlist.core.domain.Wishlist;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.http.ResponseEntity;
import org.springframework.web.bind.annotation.GetMapping;
import org.springframework.web.bind.annotation.RequestMapping;
import org.springframework.web.bind.annotation.RequestParam;
import org.springframework.web.bind.annotation.RestController;

import java.util.List;
import java.util.stream.Collectors;

@Slf4j
@RestController
@RequestMapping("/api/v2/wishlists")
@RequiredArgsConstructor
public class PublicWishlistController implements PublicWishlistV2ApiSpec {
    private final GetPublicWishlistUseCase getPublicWishlistUseCase;
    private final MemberRepository memberRepository;
    private final ProductSupport productSupport;

    // TODO 코드 제거 or 쓸거면 WishlistController 로 옮기기
    // 공개 피드 검색
    @Override
    @GetMapping("/search")
    public ResponseEntity<RsData<List<MemberWishlistSummaryResponse>>> search(
            @RequestParam(value = "nickname", required = false) String nickname
    ) {
        // 멤버 조회
        var members = (nickname == null || nickname.isBlank())
                ? memberRepository.findAll()
                : memberRepository.findByNicknameContainingIgnoreCase(nickname);

        // memberIds 추출 → PUBLIC 위시리스트 보유자 필터
        var memberIds = members.stream().map(Member::getId).toList();
        var publicWishlists = getPublicWishlistUseCase.findPublicWishlists(memberIds);
        var publicMemberIds = publicWishlists.stream().map(Wishlist::getMemberId).collect(Collectors.toSet());

        // 응답 변환 (PUBLIC만)
        var result = members.stream()
                .filter(m -> publicMemberIds.contains(m.getId()))
                .map(m -> new MemberWishlistSummaryResponse(m.getId(), m.getNickname()))
                .toList();

        return ResponseEntity.ok(RsData.success(result));
    }

    // TODO 레거시 코드 제거
    // 타인의 PUBLIC 위시리스트 상세
//    @Override
//    @GetMapping("/public/{memberId}")
//    public ResponseEntity<RsData<PublicWishlistResponse>> getPublicWishlist(
//            @PathVariable("memberId") Long memberId
//    ) {
//        List<WishlistItem> items = getPublicWishlistUseCase.getPublicWishlistItems(memberId);
//        if (items.isEmpty()) {
//            return ResponseEntity.ok(RsData.success(null));  // 비공개이거나 아이템 없음
//        }
//
//        // productId → Product 매핑 (한번에 벌크 조회)
//        var productIds = items.stream().map(WishlistItem::getProductId).toList();
//        var productMap = productSupport.findAllById(productIds).stream()
//                .collect(Collectors.toMap(Product::getId, p -> p));
//
//        // 닉네임 조회
//        String nickname = memberRepository.findById(memberId)
//                .map(Member::getNickname).orElse("알 수 없음");
//
//        // 응답 조합
//        var itemDtos = items.stream().map(item -> {
//            var product = productMap.get(item.getProductId());
//            return PublicWishlistResponse.PublicWishlistItemDto.builder()
//                    .wishlistItemId(item.getId())
//                    .productId(item.getProductId())
//                    .productName(product != null ? product.getName() : "삭제된 상품")
//                    .price(product != null ? product.getPrice() : 0)
//                    .addedAt(item.getAddedAt())
//                    .build();
//        }).toList();
//
//        var response = PublicWishlistResponse.builder()
//                .memberId(memberId)
//                .nickname(nickname)
//                .items(itemDtos)
//                .build();
//
//        return ResponseEntity.ok(RsData.success(response));
//    }
}
