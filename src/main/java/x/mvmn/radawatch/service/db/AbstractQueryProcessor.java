package x.mvmn.radawatch.service.db;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;

import org.h2.util.JdbcUtils;

public abstract class AbstractQueryProcessor {

	protected abstract DataBaseConnectionService getDBService();

	public <T> T processQuery(final String query, final ResultSetConverter<T> converter) throws Exception {
		T result = null;
		Connection conn = null;
		Statement stmt = null;
		try {
			conn = getDBService().getConnection();
			stmt = conn.createStatement();
			final ResultSet resultSet = stmt.executeQuery(query);
			result = converter.convert(resultSet);
		} finally {
			JdbcUtils.closeSilently(stmt);
			JdbcUtils.closeSilently(conn);
		}
		return result;
	}
}
