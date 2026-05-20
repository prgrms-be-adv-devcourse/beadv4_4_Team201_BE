package app.giftify.api.config.datasource;

import java.util.Map;

import javax.sql.DataSource;

import org.springframework.beans.factory.annotation.Qualifier;
import org.springframework.boot.jdbc.autoconfigure.DataSourceProperties;
import org.springframework.boot.context.properties.ConfigurationProperties;
import org.springframework.context.annotation.Bean;
import org.springframework.context.annotation.Configuration;
import org.springframework.context.annotation.Primary;
import org.springframework.context.annotation.Profile;
import org.springframework.jdbc.datasource.LazyConnectionDataSourceProxy;

import com.zaxxer.hikari.HikariDataSource;

@Configuration
@Profile("!test")
public class DataSourceConfig {

	@Bean
	@ConfigurationProperties("spring.datasource.primary")
	public DataSourceProperties primaryDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "primaryDataSource")
	@ConfigurationProperties("spring.datasource.primary.hikari")
	public HikariDataSource primaryDataSource(
		@Qualifier("primaryDataSourceProperties") DataSourceProperties props
	) {
		HikariDataSource ds = props.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		ds.setPoolName("giftify-primary");
		return ds;
	}

	@Bean
	@ConfigurationProperties("spring.datasource.replica")
	public DataSourceProperties replicaDataSourceProperties() {
		return new DataSourceProperties();
	}

	@Bean(name = "replicaDataSource")
	@ConfigurationProperties("spring.datasource.replica.hikari")
	public HikariDataSource replicaDataSource(
		@Qualifier("replicaDataSourceProperties") DataSourceProperties props
	) {
		HikariDataSource ds = props.initializeDataSourceBuilder().type(HikariDataSource.class).build();
		ds.setPoolName("giftify-replica");
		ds.setReadOnly(true);
		return ds;
	}

	@Bean
	@Primary
	public DataSource dataSource(
		@Qualifier("primaryDataSource") DataSource primary,
		@Qualifier("replicaDataSource") DataSource replica
	) {
		RoutingDataSource routing = new RoutingDataSource();
		routing.setTargetDataSources(Map.of(
			DataSourceKey.PRIMARY, primary,
			DataSourceKey.REPLICA, replica
		));
		routing.setDefaultTargetDataSource(primary);
		routing.afterPropertiesSet();
		return new LazyConnectionDataSourceProxy(routing);
	}
}
