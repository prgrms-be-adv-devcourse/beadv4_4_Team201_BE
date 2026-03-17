package giftify.support.web.util;

import app.giftify.shared.api.exception.InfraException;
import tools.jackson.core.JacksonException;
import tools.jackson.databind.ObjectMapper;
import tools.jackson.databind.json.JsonMapper;
import giftify.support.web.idempotency.util.PayloadHasher;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;
import org.junit.jupiter.api.extension.ExtendWith;
import org.mockito.junit.jupiter.MockitoExtension;

import static org.assertj.core.api.Assertions.assertThat;
import static org.assertj.core.api.Assertions.assertThatThrownBy;
import static org.mockito.ArgumentMatchers.any;
import static org.mockito.Mockito.mock;
import static org.mockito.Mockito.when;

@ExtendWith(MockitoExtension.class)
class PayloadHasherTest {

    private PayloadHasher payloadHasher;
    private ObjectMapper objectMapper;

    @BeforeEach
    void setUp() {
        objectMapper = JsonMapper.builder().build();
        payloadHasher = new PayloadHasher(objectMapper);
    }

    @Test
    @DisplayName("성공: 동일한 객체는 동일한 해시값을 반환한다")
    void calculateHash_success_same_payload() {
        // given
        TestDto dto1 = new TestDto("giftify", 1000);
        TestDto dto2 = new TestDto("giftify", 1000);

        // when
        String hash1 = payloadHasher.calculateHash(dto1);
        String hash2 = payloadHasher.calculateHash(dto2);

        // then
        assertThat(hash1).isEqualTo(hash2);
        assertThat(hash1).hasSize(64); // SHA-256은 64자리 16진수
    }

    @Test
    @DisplayName("성공: 데이터가 다르면 해시값도 달라야 한다")
    void calculateHash_different_payload() {
        // given
        TestDto dto1 = new TestDto("userA", 100);
        TestDto dto2 = new TestDto("userB", 100);

        // when
        String hash1 = payloadHasher.calculateHash(dto1);
        String hash2 = payloadHasher.calculateHash(dto2);

        // then
        assertThat(hash1).isNotEqualTo(hash2);
    }

    @Test
    @DisplayName("성공: 페이로드가 null이면 빈 문자열을 반환한다")
    void calculateHash_null_payload() {
        // when
        String hash = payloadHasher.calculateHash(null);

        // then
        assertThat(hash).isEmpty();
    }

    @Test
    @DisplayName("실패: 직렬화 실패 시 InfraException이 발생한다")
    void calculateHash_serialization_fail() throws JacksonException {
        // given: Mock ObjectMapper를 사용하여 예외 강제 발생
        ObjectMapper mockMapper = mock(ObjectMapper.class);
        PayloadHasher failHasher = new PayloadHasher(mockMapper);

        when(mockMapper.writeValueAsString(any()))
                .thenThrow(new JacksonException("Error") {});

        // when & then
        assertThatThrownBy(() -> failHasher.calculateHash(new Object()))
                .isInstanceOf(InfraException.class);
    }

    // 테스트용 DTO
    static class TestDto {
        private String name;
        private int price;

        public TestDto(String name, int price) {
            this.name = name;
            this.price = price;
        }
        // Jackson 직렬화를 위해 Getter 필요
        public String getName() { return name; }
        public int getPrice() { return price; }
    }
}