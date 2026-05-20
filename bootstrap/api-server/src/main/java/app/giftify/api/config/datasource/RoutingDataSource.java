package app.giftify.api.config.datasource;

import org.springframework.jdbc.datasource.lookup.AbstractRoutingDataSource;

public class RoutingDataSource extends AbstractRoutingDataSource {

	@Override
	protected DataSourceKey determineCurrentLookupKey() {
		DataSourceKey key = DataSourceContextHolder.get();
		return key != null ? key : DataSourceKey.PRIMARY;
	}
}
