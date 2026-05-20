package app.giftify.security.common.annotation;

import java.lang.annotation.Documented;
import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

/**
 * 내부 서비스 간 통신용 API임을 표시하는 marker 어노테이션.
 *
 * <p>실제 인증은 {@link app.giftify.security.internal.InternalApiAuthFilter}
 * 가 URI prefix {@code /api/internal/**} 매칭으로 수행한다. 이 어노테이션은
 * 코드 검색/문서/감사용 marker 역할만 한다.</p>
 *
 * <h3>보안 고려사항</h3>
 * <ul>
 *   <li>실제 검증은 {@code X-Internal-Api-Key} 헤더 + 시크릿 상수시간 비교.</li>
 *   <li>프로덕션 환경에서는 네트워크 레벨 보안(VPC, Security Group 등)을 병행해야 한다.</li>
 *   <li>시크릿은 {@code giftify.security.internal-api-key} 프로퍼티 (환경변수 또는 SOPS).</li>
 * </ul>
 *
 * @see app.giftify.security.internal.InternalApiAuthFilter
 */
@Target({ElementType.METHOD, ElementType.TYPE})
@Retention(RetentionPolicy.RUNTIME)
@Documented
public @interface InternalApiOnly {
}
