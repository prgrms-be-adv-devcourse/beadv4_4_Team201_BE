package app.giftify.support.common.event.auth;

import lombok.Getter;
import org.springframework.context.ApplicationEvent;

@Getter
public class UserAuthenticatedEvent extends ApplicationEvent {
    private final String authSub;
    private final String nickname;
    private final String email;
    private final String name;

    public UserAuthenticatedEvent(Object source, String authSub, String nickname, String email, String name) {
        super(source);

        this.authSub = authSub;
        this.nickname = nickname;
        this.email = email;
        this.name = name;
    }
}
