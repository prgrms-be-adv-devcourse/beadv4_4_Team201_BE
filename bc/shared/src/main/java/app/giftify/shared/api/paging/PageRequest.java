package app.giftify.shared.api.paging;

import com.fasterxml.jackson.annotation.JsonIgnore;

public record PageRequest(int page, int size) {

    public PageRequest {
        if (page < 0) {
            throw new IllegalArgumentException("페이지 번호는 0보다 작을 수 없습니다.");
        }
        if (size <= 0) {
            throw new IllegalArgumentException("페이지 크기는 0보다 커야 합니다.");
        }
    }

    public static PageRequest of(int page, int size) {
        return new PageRequest(page, size);
    }

    /**
     * 기본 페이지 요청 (0페이지, 10개씩)
     */
    public static PageRequest defaultRequest() {
        return new PageRequest(0, 10);
    }

    @JsonIgnore
    public long getOffset() {
        return (long) page * size;
    }
}
