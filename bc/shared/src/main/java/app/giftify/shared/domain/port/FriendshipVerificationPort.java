package app.giftify.shared.domain.port;

/**
 * 다른 모듈의 친구 관계를 확인하기 위한 Port 인터페이스
 *
 * <b>아키텍처적 역할:</b>
 * 이 인터페이스는 모듈 간의 경계를 넘어 통신하기 위한 Port입니다.
 * 예를 들어, {@code funding} 모듈에서 친구의 펀딩을 조회하기 전,
 * {@code member} 모듈의 친구 관계 데이터를 확인해야 할 때 사용됩니다.
 * @see "app.giftify.friendship.adapter.out.FriendshipVerificationAdapter"
 */
public interface FriendshipVerificationPort {

    /**
     * 두 사용자가 서로 친구 관계(수락 상태)인지 확인합니다.
     *
     * @param memberId 요청하는 사용자의 ID
     * @param friendId 확인 대상 사용자의 ID
     * @return 친구 관계가 맞으면 {@code true}, 아니면 {@code false}
     */
    boolean areFriends(Long memberId, Long friendId);
}
