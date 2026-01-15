package app.giftify.support.common.event.auth;

import org.springframework.context.ApplicationEvent;

import lombok.Getter;

/**
 * 인증 성공 스프링 이벤트.
 */
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
