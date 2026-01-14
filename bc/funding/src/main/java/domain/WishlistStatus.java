package domain;

/**
 * 위시리스트 아이템 상태
 */
public enum WishlistStatus {
    PENDING,                // 활성 (위시리스트에 담긴 상태)
    IN_PROGRESS,            // 펀딩 진행 중
    REQUESTED_CONFIRM,      // 수령자 확정 대기 중
    COMPLETED               // 수령자 확정 완료

}

