package app.giftify.support.jpa;

import app.giftify.shared.api.paging.PageRequest;
import org.springframework.data.domain.Pageable;
import org.springframework.data.domain.Sort;

/**
 * Shared Kernel의 PageRequest를 Spring Data JPA의 Pageable로 변환하는 유틸리티
 */
public final class PageConverter {

    private PageConverter() {
    }

    public static Pageable toPageable(PageRequest request) {
        return org.springframework.data.domain.PageRequest.of(
            request.page(),
            request.size(),
            Sort.unsorted() // 기본 정렬 없음
        );
    }

    public static Pageable toPageable(PageRequest request, Sort sort) {
        return org.springframework.data.domain.PageRequest.of(
            request.page(),
            request.size(),
            sort
        );
    }

    public static PageRequest toPageRequest(Pageable pageable) {
        return PageRequest.of(pageable.getPageNumber(), pageable.getPageSize());
    }
}
