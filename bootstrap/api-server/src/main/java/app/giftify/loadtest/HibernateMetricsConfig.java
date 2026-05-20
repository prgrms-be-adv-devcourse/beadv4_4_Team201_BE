package app.giftify.loadtest;

import io.micrometer.core.instrument.MeterRegistry;
import io.micrometer.core.instrument.Tags;
import jakarta.persistence.EntityManagerFactory;
import org.hibernate.SessionFactory;
import org.hibernate.stat.HibernateMetrics;
import org.springframework.boot.autoconfigure.condition.ConditionalOnProperty;
import org.springframework.context.annotation.Configuration;

@Configuration
@ConditionalOnProperty(name = "spring.jpa.properties.hibernate.generate_statistics", havingValue = "true")
public class HibernateMetricsConfig {

    public HibernateMetricsConfig(EntityManagerFactory emf, MeterRegistry registry) {
        SessionFactory sf = emf.unwrap(SessionFactory.class);
        new HibernateMetrics(sf, "default", Tags.empty()).bindTo(registry);
    }
}
