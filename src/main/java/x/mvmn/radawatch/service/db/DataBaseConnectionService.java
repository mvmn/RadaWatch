package x.mvmn.radawatch.service.db;

import java.io.File;
import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.List;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

public class DataBaseConnectionService {

	protected final JdbcConnectionPool connectionPool;

	public DataBaseConnectionService() {
		try {
			Class.forName("org.h2.Driver");
		} catch (ClassNotFoundException e) {
			throw new RuntimeException(e);
		}
		String userHome = System.getProperty("user.home");
		File appHomeFolder = new File(userHome + File.separator + ".radawatch");
		appHomeFolder.mkdirs();
		connectionPool = JdbcConnectionPool.create("jdbc:h2:~/.radawatch/test;MODE=MySQL;IGNORECASE=TRUE;", "sa", "");
	}

	public Connection getConnection() throws SQLException {
		return connectionPool.getConnection();
	}

	public void dispose() {
		this.connectionPool.dispose();
	}

	public void execSingleStatement(String sqlStatement) throws SQLException {
		Connection connection = null;
		try {
			connection = getConnection();
			connection.createStatement().execute(sqlStatement);
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception connectionClosingException) {
					connectionClosingException.printStackTrace();
				}
			}
		}
	}

	public Timestamp execSelectOneDate(String sqlStatement) throws SQLException {
		Timestamp result = null;
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery(sqlStatement);
			if (resultSet != null && resultSet.next()) {
				result = resultSet.getTimestamp(1);
			}
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception connectionClosingException) {
					connectionClosingException.printStackTrace();
				}
			}
		}
		return result;
	}

	public String execSelectOneString(String sqlStatement) throws SQLException {
		String result = null;
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery(sqlStatement);
			if (resultSet != null && resultSet.next()) {
				result = resultSet.getString(1);
			}
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception connectionClosingException) {
					connectionClosingException.printStackTrace();
				}
			}
		}
		return result;
	}

	public int execSelectOneInt(String sqlStatement) throws SQLException {
		int result = 0;
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery(sqlStatement);
			if (resultSet != null && resultSet.next()) {
				result = resultSet.getInt(1);
			}
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception connectionClosingException) {
					connectionClosingException.printStackTrace();
				}
			}
		}
		return result;
	}

	public void openDbBrowser() {
		new Thread() {
			public void run() {
				Connection conn = null;
				try {
					Server.startWebServer(DataBaseConnectionService.this.getConnection());
				} catch (SQLException e) {
					e.printStackTrace();
				} finally {
					if (conn != null) {
						try {
							conn.close();
						} catch (Exception e) {
						}
					}
				}
			}
		}.start();
	}

	public List<String> execSelectOneColumn(String sqlStatement) throws Exception {
		List<String> result = new ArrayList<String>();
		Connection connection = null;
		try {
			connection = getConnection();
			Statement stmt = connection.createStatement();
			ResultSet resultSet = stmt.executeQuery(sqlStatement);
			if (resultSet != null) {
				while (resultSet.next()) {
					result.add(resultSet.getString(1));
				}
			}
		} finally {
			if (connection != null) {
				try {
					connection.close();
				} catch (Exception connectionClosingException) {
					connectionClosingException.printStackTrace();
				}
			}
		}
		return result;
	}
}
