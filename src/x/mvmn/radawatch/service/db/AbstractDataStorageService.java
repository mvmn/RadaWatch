package x.mvmn.radawatch.service.db;

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
		dbService.execSingleStatement("create table " + tableName + (ifNotExists ? " if not exists (" : " (") + getTableDefinitionSql(tableName) + ");");
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
}
