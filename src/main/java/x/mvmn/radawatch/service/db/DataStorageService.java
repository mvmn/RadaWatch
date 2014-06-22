package x.mvmn.radawatch.service.db;

import java.util.Map;

import x.mvmn.radawatch.model.Entity;

public interface DataStorageService<T extends Entity> {

	public String[] getTablesNames();

	public String getTableDefinitionSql(String tableName);

	public void dropTable(String tableName, boolean ifExists) throws Exception;

	public void createTable(String tableName, boolean ifNotExists) throws Exception;

	public void dropAllTables() throws Exception;

	public void createAllTables() throws Exception;

	public void storeNewRecord(final T data) throws Exception;

	public boolean checkExists(int itemId) throws Exception;

	public Map<String, String> getStats() throws Exception;

}
