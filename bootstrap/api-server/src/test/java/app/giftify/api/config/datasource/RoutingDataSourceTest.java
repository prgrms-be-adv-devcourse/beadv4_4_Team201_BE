package app.giftify.api.config.datasource;

import static org.assertj.core.api.Assertions.assertThat;
import static org.mockito.Mockito.mock;

import java.util.Map;

import javax.sql.DataSource;

import org.junit.jupiter.api.AfterEach;
import org.junit.jupiter.api.BeforeEach;
import org.junit.jupiter.api.DisplayName;
import org.junit.jupiter.api.Test;

class RoutingDataSourceTest {

	private RoutingDataSource routingDataSource;
	private DataSource primary;
	private DataSource replica;

	@BeforeEach
	void setUp() {
		primary = mock(DataSource.class);
		replica = mock(DataSource.class);
		routingDataSource = new RoutingDataSource();
		routingDataSource.setTargetDataSources(Map.of(
			DataSourceKey.PRIMARY, primary,
			DataSourceKey.REPLICA, replica
		));
		routingDataSource.setDefaultTargetDataSource(primary);
		routingDataSource.afterPropertiesSet();
	}

	@AfterEach
	void tearDown() {
		DataSourceContextHolder.clear();
	}

	@Test
	@DisplayName("context 가 비어 있으면 PRIMARY 키가 결정된다")
	void emptyContextResolvesToPrimary() {
		assertThat(routingDataSource.determineCurrentLookupKey()).isEqualTo(DataSourceKey.PRIMARY);
	}

	@Test
	@DisplayName("REPLICA context 가 설정되면 REPLICA 키가 결정된다")
	void replicaContextResolvesToReplica() {
		DataSourceContextHolder.set(DataSourceKey.REPLICA);
		assertThat(routingDataSource.determineCurrentLookupKey()).isEqualTo(DataSourceKey.REPLICA);
	}

	@Test
	@DisplayName("PRIMARY context 가 설정되면 PRIMARY 키가 결정된다")
	void primaryContextResolvesToPrimary() {
		DataSourceContextHolder.set(DataSourceKey.PRIMARY);
		assertThat(routingDataSource.determineCurrentLookupKey()).isEqualTo(DataSourceKey.PRIMARY);
	}
}
