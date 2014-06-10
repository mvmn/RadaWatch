package x.mvmn.radawatch.service.db.presdecrees;

import x.mvmn.radawatch.service.db.AbstractDataStorageService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesStorageService extends AbstractDataStorageService {

	private static final String SQL_TABLES[] = new String[] { "presidentialdecree" };
	private static final String SQL_TABLE_PRESIDENTIALDECREE_DEFINITION = "id int not null primary key auto_increment, reldate TIMESTAMP, g_id int, type varchar(1024), numcode varchar(1024), title varchar(16384), text varchar(65536)";

	public PresidentialDecreesStorageService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	@Override
	public String[] getTablesNames() {
		return SQL_TABLES;
	}

	@Override
	public String getTableDefinitionSql(String tableName) {
		return SQL_TABLE_PRESIDENTIALDECREE_DEFINITION;
	}

}
