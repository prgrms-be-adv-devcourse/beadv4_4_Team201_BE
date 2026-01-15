package event.auth;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// 인증 성공 이벤트를 공유 모듈에서 관리하여 모든 모듈이 구독할 수 있도록 구현.
@Getter
public class UserAuthenticatedEvent extends ApplicationEvent {
    private final String sub;
    private final String email;
    private final String name;

    public UserAuthenticatedEvent(Object source, String sub, String email, String name) {
        super(source);
        this.sub = sub;
        this.email = email;
        this.name = name;
    }
}
