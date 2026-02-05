package app.giftify.security.common.context;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 인증된 사용자의 식별자를 주입받기 위한 어노테이션.
 *
 * @deprecated 2세대 어노테이션으로 마이그레이션 필요:
 *             <ul>
 *               <li>{@code Long memberId} → {@link app.giftify.security.common.CurrentMemberId @CurrentMemberId}</li>
 *               <li>{@code String authSub} → {@link app.giftify.security.common.CurrentAuthSub @CurrentAuthSub}</li>
 *               <li>전체 정보 → {@code @AuthenticationPrincipal MemberPrincipal}</li>
 *             </ul>
 * @see app.giftify.security.common.CurrentMemberId
 * @see app.giftify.security.common.CurrentAuthSub
 */
@Deprecated(since = "2026.02", forRemoval = true)
@Target(ElementType.PARAMETER)
@Retention(RetentionPolicy.RUNTIME)
public @interface AuthenticatedMember {
    String value() default "";
}
