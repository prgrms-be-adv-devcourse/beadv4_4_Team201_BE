package app.giftify.security.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

import org.springframework.security.access.prepost.PreAuthorize;

/**
 * 내부 서비스 간 통신용 API임을 표시하는 어노테이션.
 *
 * <p>이 어노테이션이 적용된 메서드/클래스는 {@code ROLE_INTERNAL_SERVICE} 역할을 가진
 * 인증 주체만 호출할 수 있습니다.</p>
 *
 * <h3>보안 고려사항</h3>
 * <ul>
 *   <li>이 어노테이션은 애플리케이션 레벨 보안만 제공합니다.</li>
 *   <li>프로덕션 환경에서는 네트워크 레벨 보안(VPC, Security Group 등)을 병행해야 합니다.</li>
 *   <li>{@code ROLE_INTERNAL_SERVICE}는 서비스 계정 토큰에서만 부여되어야 합니다.</li>
 * </ul>
 *
 * @see org.springframework.security.access.prepost.PreAuthorize
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
@PreAuthorize("hasRole('INTERNAL_SERVICE')")
public @interface InternalApiOnly {
}
