package app.giftify.member.application.service;

import app.giftify.member.application.port.out.MemberRepositoryPort;
import app.giftify.member.domain.member.NicknameGenerator;
import lombok.RequiredArgsConstructor;
import lombok.extern.slf4j.Slf4j;
import org.springframework.stereotype.Component;

import java.util.List;
import java.util.Random;

@Slf4j
@Component
@RequiredArgsConstructor
public class RandomNicknameGenerator implements NicknameGenerator {
    private static final List<String> adjectives = List.of("행복한", "똑똑한", "친절한", "나태한", "과감한", "수줍은", "지루한");
    private static final List<String> animals = List.of("강아지", "고양이", "앵무새", "돼지", "코요테", "코끼리", "호랑이");
    private static final int RANDOM_DIGITS = 4;
    private static final int MAX_ATTEMPTS = 10;
    private static final Random RANDOM = new Random();

    private final MemberRepositoryPort memberRepositoryPort;

    @Override
    public String generate() {
        for (int attempt = 0; attempt < MAX_ATTEMPTS; attempt++) {
            String nickname = generateRandomNickname();

            if (memberRepositoryPort.findByNickname(nickname).isEmpty()) {
                log.debug("[NicknameGenerator] 닉네임 생성 성공: {}", nickname);
                return nickname;
            }
            log.debug("[NicknameGenerator] 닉네임 중복, 재시도: {} (시도 {})", nickname, attempt + 1);
        }

        // Fallback: 타임스탬프 기반 닉네임
        String fallback = generateRandomNicknamePrefix() + System.currentTimeMillis() % 100000;
        log.warn("[NicknameGenerator] 최대 시도 초과, fallback 사용: {}", fallback);

        return fallback;
    }

    private String generateRandomNickname() {
        int randomNumber = RANDOM.nextInt((int) Math.pow(10, RANDOM_DIGITS));

        return generateRandomNicknamePrefix() + randomNumber;
    }

    private String generateRandomNicknamePrefix() {
        String adj = adjectives.get(RANDOM.nextInt(adjectives.size()));
        String animal = animals.get(RANDOM.nextInt(animals.size()));

        return adj + animal;
    }
}
