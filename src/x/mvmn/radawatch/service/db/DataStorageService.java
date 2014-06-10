package x.mvmn.radawatch.service.db;

public interface DataStorageService {

	public String[] getTablesNames();

	public String getTableDefinitionSql(String tableName);

	public void dropTable(String tableName, boolean ifExists) throws Exception;

	public void createTable(String tableName, boolean ifNotExists) throws Exception;

	public void dropAllTables() throws Exception;

	public void createAllTables() throws Exception;

}
