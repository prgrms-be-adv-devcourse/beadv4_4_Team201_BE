package giftify.support.web.idempotency.util;

import app.giftify.shared.api.exception.InfraErrorCode;
import app.giftify.shared.api.exception.InfraException;
import tools.jackson.core.JsonProcessingException;
import tools.jackson.databind.ObjectMapper;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.nio.charset.StandardCharsets;
import java.security.MessageDigest;
import java.security.NoSuchAlgorithmException;

@Slf4j
@Component
@RequiredArgsConstructor
public class PayloadHasher {

    private final ObjectMapper objectMapper;

    /**
     * 객체를 JSON으로 변환 후 SHA-256 해시를 생성
     */
    public String calculateHash(Object payload) {
        if (payload == null) {
            return ""; // 페이로드가 없는 GET/DELETE 등의 요청 처리용
        }

        try {
            String jsonPayload = objectMapper.writeValueAsString(payload);

            MessageDigest digest = MessageDigest.getInstance("SHA-256");
            byte[] encodedHash = digest.digest(jsonPayload.getBytes(StandardCharsets.UTF_8));

            return bytesToHex(encodedHash);

        } catch (JsonProcessingException e) {
            log.error("페이로드 직렬화 실패", e);
            throw new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR);
        } catch (NoSuchAlgorithmException e) {
            log.error("SHA-256 알고리즘을 찾을 수 없음", e);
            throw new InfraException(InfraErrorCode.UNKNOWN_INFRA_ERROR);
        }
    }

    public boolean isMatch(String hash, String other) {
        return hash.equals(other);
    }

    private String bytesToHex(byte[] hash) {
        StringBuilder hexString = new StringBuilder(2 * hash.length);
        for (byte b : hash) {
            String hex = Integer.toHexString(0xff & b);
            if (hex.length() == 1) {
                hexString.append('0');
            }
            hexString.append(hex);
        }
        return hexString.toString();
    }
}