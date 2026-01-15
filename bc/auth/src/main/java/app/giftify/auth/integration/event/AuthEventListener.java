package app.giftify.auth.integration.event;

import event.auth.UserAuthenticatedEvent;
import org.springframework.context.event.EventListener;
import org.springframework.stereotype.Component;

// 멤버 모듈과의 비동기 협업을 위한 이벤트 구조
@Component
public class AuthEventListener {

    @EventListener
    public void handleUserAuthenticated(UserAuthenticatedEvent event) {
        // 인증 성공 사건을 인지함
        System.out.println("===== 인증 이벤트 수신 =====");
        System.out.println("ID: " + event.getSub());
        System.out.println("Email: " + event.getEmail());

        System.out.println("Kafka Topic [member-signup-topic]으로 메시지 발송 완료");
        System.out.println("이 정보를 Member 모듈로 전송할 준비를 합니다.");

        // TODO: 여기서 외부 브로커(Kafka 등)로 메시지 발행
    }
}