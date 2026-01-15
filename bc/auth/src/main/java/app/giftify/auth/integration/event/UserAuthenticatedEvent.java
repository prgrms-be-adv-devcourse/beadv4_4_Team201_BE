package app.giftify.auth.integration.event;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

// 멤버 모듈과의 비동기 협업을 위한 이벤트 구조
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
