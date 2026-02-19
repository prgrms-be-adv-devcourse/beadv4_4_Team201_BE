package app.giftify.support.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface Idempotent {
    String prefix() default "COMMON"; // 도메인별 구분자 (예: FUNDING, ORDER)
    long ttl() default 10;            // 멱등성 유지 시간 (분)
}