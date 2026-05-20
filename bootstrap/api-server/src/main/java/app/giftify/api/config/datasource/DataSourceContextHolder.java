package app.giftify.api.config.datasource;

public final class DataSourceContextHolder {

	private static final ThreadLocal<DataSourceKey> CONTEXT = new ThreadLocal<>();

	private DataSourceContextHolder() {
	}

	public static void set(DataSourceKey key) {
		CONTEXT.set(key);
	}

	public static DataSourceKey get() {
		return CONTEXT.get();
	}

	public static void clear() {
		CONTEXT.remove();
	}
}
