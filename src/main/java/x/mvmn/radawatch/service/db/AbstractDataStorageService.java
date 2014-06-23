package x.mvmn.radawatch.service.db;

import java.util.LinkedHashMap;
import java.util.Map;

import x.mvmn.radawatch.model.Entity;

public abstract class AbstractDataStorageService<T extends Entity> implements DataStorageService<T> {

	protected final DataBaseConnectionService dbService;

	public AbstractDataStorageService(final DataBaseConnectionService dbService) {
		this.dbService = dbService;
	}

	@Override
	public void dropTable(String tableName, boolean ifExists) throws Exception {
		dbService.execSingleStatement("drop table " + tableName + (ifExists ? " if exists;" : ";"));
	}

	@Override
	public void createTable(String tableName, boolean ifNotExists) throws Exception {
		dbService.execSingleStatement("create table if not exists " + tableName + (ifNotExists ? " (" : " (") + getTableDefinitionSql(tableName) + ");");
	}

	@Override
	public void dropAllTables() throws Exception {
		for (final String tableName : getTablesNames()) {
			dropTable(tableName, true);
		}
	}

	@Override
	public void createAllTables() throws Exception {
		for (final String tableName : getTablesNames()) {
			createTable(tableName, true);
		}
	}

	public Map<String, String> getStats() throws Exception {
		final Map<String, String> results = new LinkedHashMap<String, String>();

		for (final String tableName : getTablesNames()) {
			int count = dbService.execSelectCount("select count(*) from " + tableName);
			results.put("DB Table rows: " + tableName, String.valueOf(count));
		}

		return results;
	}
}
