package app.giftify.support.common.api.paging;

import java.util.List;

/**
 * 페이징 조회 결과를 담는 인터페이스.
 * Repository 계층에서 페이징된 도메인 객체 목록을 반환하는 용도입니다.
 *
 * @param <T> 조회 결과 타입
 */
public record Page<T>(
    List<T> content,
    long totalElements
) {
    /**
     * Page 인스턴스를 생성합니다.
     *
     * @param content 조회된 컨텐츠 목록
     * @param totalElements 전체 요소 개수
     * @param <T> 컨텐츠 타입
     * @return Page 인스턴스
     */
    public static <T> Page<T> of(List<T> content, long totalElements) {
        return new Page<>(content, totalElements);
    }
}
