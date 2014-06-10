package x.mvmn.radawatch.service.db.presdecrees;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.service.db.AbstractDataStorageService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesStorageService extends AbstractDataStorageService<PresidentialDecree> {

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

	@Override
	public void storeNewRecord(final PresidentialDecree data) throws Exception {
		// TODO Auto-generated method stub
	}

	@Override
	public boolean checkExists(int decreeId) throws Exception {
		boolean result = true;
		Connection conn = null;
		try {
			conn = dbService.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select * from " + SQL_TABLES[0] + " where g_id = " + decreeId);
			ResultSet rs = stmt.getResultSet();
			result = rs.next();
		} finally {
			JdbcUtils.closeSilently(conn);
		}
		return result;
	}

}
