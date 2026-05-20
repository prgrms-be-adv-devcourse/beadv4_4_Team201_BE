package app.giftify.api.config.datasource;

import org.aspectj.lang.ProceedingJoinPoint;
import org.aspectj.lang.annotation.Around;
import org.aspectj.lang.annotation.Aspect;
import org.springframework.core.annotation.Order;
import org.springframework.stereotype.Component;
import org.springframework.transaction.annotation.Transactional;

@Aspect
@Component
@Order(0)
public class DataSourceRoutingAspect {

	@Around("@annotation(transactional) || @within(transactional)")
	public Object routeByTransactional(ProceedingJoinPoint pjp, Transactional transactional) throws Throwable {
		DataSourceKey previous = DataSourceContextHolder.get();
		DataSourceKey next = transactional.readOnly() ? DataSourceKey.REPLICA : DataSourceKey.PRIMARY;
		DataSourceContextHolder.set(next);
		try {
			return pjp.proceed();
		} finally {
			if (previous == null) {
				DataSourceContextHolder.clear();
			} else {
				DataSourceContextHolder.set(previous);
			}
		}
	}
}
