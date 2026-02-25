package app.giftify.support.common.annotation;

import java.lang.annotation.ElementType;
import java.lang.annotation.Retention;
import java.lang.annotation.RetentionPolicy;
import java.lang.annotation.Target;

@Target(ElementType.METHOD)
@Retention(RetentionPolicy.RUNTIME)
public @interface EventIdempotent {
	String prefix() default "EVENT_IDEM";
	long ttl() default 60; 				// 멱등성 유지 시간 (분)
}
