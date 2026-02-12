package app.giftify.security.common.util;

import app.giftify.security.common.MemberPrincipal;
import org.springframework.security.core.Authentication;
import org.springframework.security.core.context.SecurityContextHolder;

import java.util.Optional;

public class SecurityUtil {

    private SecurityUtil() {}

    public static Optional<Long> getCurrentMemberId() {
        Authentication auth = SecurityContextHolder.getContext().getAuthentication();

        if (auth == null || !auth.isAuthenticated()) {
            return Optional.empty();
        }

        Object principal = auth.getPrincipal();

        if (principal instanceof MemberPrincipal memberPrincipal) {
            return Optional.of(memberPrincipal.memberId());
        }

        return Optional.empty();
    }
}