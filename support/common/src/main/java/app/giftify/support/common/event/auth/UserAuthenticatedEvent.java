package app.giftify.support.common.event.auth;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserAuthenticatedEvent extends ApplicationEvent {
    private final String email;
    private final String name;
    private final String nickname;

    public UserAuthenticatedEvent(Object source, String nickname, String email, String name) {
        super(source);
        this.email = email;
        this.name = name;
        this.nickname = nickname;
    }
}
