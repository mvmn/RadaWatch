package x.mvmn.radawatch.service.db.presdecrees;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.Map;

import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.service.db.AbstractDataStorageService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesStorageService extends AbstractDataStorageService<PresidentialDecree> {

	private static final String SQL_TABLES[] = new String[] { "presidentialdecree" };
	private static final String SQL_TABLE_PRESIDENTIALDECREE_DEFINITION = "id int not null primary key auto_increment, g_id int, reldate TIMESTAMP, decreetype varchar(1024), numcode varchar(1024), title varchar(16384), completetext TEXT, unique index presidentialdecree_g_id(g_id), index presidentialdecree_reldate(reldate), index presidentialdecree_title(title(128))";

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
		Connection conn = null;
		try {
			conn = dbService.getConnection();
			PreparedStatement statement = conn.prepareStatement("insert into " + SQL_TABLES[0]
					+ " (g_id, reldate, decreetype, numcode, title, completetext) values(?, ?, ?, ?, ?, ?)");
			statement.setInt(1, data.getSiteId());
			statement.setDate(2, data.getDate());
			statement.setString(3, data.getType());
			statement.setString(4, data.getNumberCode());
			statement.setString(5, data.getTitle());
			statement.setString(6, data.getFullText());
			statement.executeUpdate();
		} finally {
			JdbcUtils.closeSilently(conn);
		}
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

	@Override
	public Map<String, String> getStats() throws Exception {
		Map<String, String> result = super.getStats();

		for (final String countByType : dbService
				.execSelectOneColumn("select concat(decreetype, ':#delim#:', count(*)) from presidentialdecree group by decreetype order by decreetype")) {
			final String[] res = countByType.split(":#delim#:");
			result.put("Decrees of type \"" + res[0] + "\"", res[1]);
		}

		return result;
	}
}
