package x.mvmn.radawatch.service.db;

import java.io.File;
import java.sql.Connection;
import java.sql.SQLException;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.Server;

public class StorageService {

	private static final String SQL_DROP_TABLES = "drop table votesession if exists; \n drop table votesessionfaction if exists; \n drop table individualvote if exists;";
	private static final String SQL_CREATE_TABLES = "create table votesession ( id int not null primary key auto_increment, g_id int not null, votetitle varchar(16384), votedate TIMESTAMP, votedyes int, votedno int, abstained int, skipped int, total int, votepassed BOOL);"
			+ "\n create table votesessionfaction ( id int not null primary key auto_increment, votesessionid int, title varchar(16384), totalmembers int, votedyes int, votedno int, abstained int, skipped int, absent int);"
			+ "\n create table individualvote ( id int not null primary key auto_increment, votesessionid int, votesessionfactionid int, name varchar(16384), voted varchar(1024));";

	protected final JdbcConnectionPool connectionPool;

	public StorageService() {
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

	public void dropTables() throws SQLException {
		for (final String statement : SQL_DROP_TABLES.split("\n")) {
			execSingleStatement(statement);
		}
	}

	public void createTables() throws SQLException {
		for (final String statement : SQL_CREATE_TABLES.split("\n")) {
			execSingleStatement(statement);
		}
	}

	public void openDbBrowser() {
		new Thread() {
			public void run() {
				Connection conn = null;
				try {
					Server.startWebServer(StorageService.this.getConnection());
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
}
