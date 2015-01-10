package x.mvmn.radawatch.service.db;

import java.awt.Component;
import java.io.File;
import java.io.FileOutputStream;
import java.io.FileReader;
import java.io.OutputStream;
import java.lang.reflect.Method;
import java.sql.Connection;
import java.sql.DriverManager;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.sql.DataSource;
import javax.swing.JFileChooser;
import javax.swing.JOptionPane;
import javax.swing.SwingUtilities;

import org.h2.jdbcx.JdbcConnectionPool;
import org.h2.tools.RunScript;
import org.h2.tools.Script;
import org.h2.tools.Server;
import org.h2.util.IOUtils;
import org.h2.util.JdbcUtils;

import com.mchange.v2.c3p0.ComboPooledDataSource;
import com.mysql.jdbc.Driver;

public class DataBaseConnectionService {

	protected final DataSource dataSource;
	protected final Runnable disposeOperation;
	protected final String[] dbOpsNames;
	protected final String dbInfo;
	protected final Map<String, Runnable> dbOps;

	public DataBaseConnectionService(final boolean useEmbeddedDb, final String host, final Integer port, final String dbName, final String login,
			final String password, final Component parentUiComponent) {
		if (useEmbeddedDb) {
			try {
				Class.forName("org.h2.Driver");
			} catch (ClassNotFoundException e) {
				throw new RuntimeException(e);
			}
			final String userHome = System.getProperty("user.home");
			final File appHomeFolder = new File(userHome + File.separator + ".radawatch");
			appHomeFolder.mkdirs();
			final JdbcConnectionPool h2ConnectionPool = JdbcConnectionPool.create("jdbc:h2:~/.radawatch/test;MODE=MySQL;IGNORECASE=TRUE;", "sa", "");
			disposeOperation = new Runnable() {
				@Override
				public void run() {
					h2ConnectionPool.dispose();
				}
			};
			dataSource = h2ConnectionPool;
			dbInfo = "H2 DB @ " + appHomeFolder.getAbsolutePath();
			dbOpsNames = new String[] { "Browse DB", "Backup DB", "Restore DB" };
			final Map<String, Runnable> dbOpsMap = new HashMap<String, Runnable>();
			dbOpsMap.put(dbOpsNames[0], new Runnable() {
				@Override
				public void run() {
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
			});
			dbOpsMap.put(dbOpsNames[1], new Runnable() {
				@Override
				public void run() {
					JFileChooser fileChooser = new JFileChooser();
					if (JFileChooser.APPROVE_OPTION == fileChooser.showSaveDialog(parentUiComponent)) {
						final File fileToSaveTo = fileChooser.getSelectedFile();
						parentUiComponent.setEnabled(false);
						new Thread() {
							public void run() {
								FileOutputStream fis = null;
								try {
									fis = new FileOutputStream(fileToSaveTo);
									Method m = Script.class.getDeclaredMethod("process", Connection.class, OutputStream.class);
									m.setAccessible(true);
									m.invoke(null, DataBaseConnectionService.this.getConnection(), fis);
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											parentUiComponent.setEnabled(true);
											JOptionPane.showMessageDialog(parentUiComponent, "File " + fileToSaveTo.getPath() + " saved successfully",
													"DB backup succeeded", JOptionPane.INFORMATION_MESSAGE);
										}
									});
								} catch (final Exception ex) {
									ex.printStackTrace();
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											parentUiComponent.setEnabled(true);
											JOptionPane.showMessageDialog(parentUiComponent,
													"DB backup failed: " + ex.getClass().getCanonicalName() + " " + ex.getMessage(), "Error occurred",
													JOptionPane.ERROR_MESSAGE);
										}
									});
								} finally {
									IOUtils.closeSilently(fis);
								}
							}
						}.start();
					}
				}
			});
			dbOpsMap.put(dbOpsNames[2], new Runnable() {
				@Override
				public void run() {
					if (JOptionPane.OK_OPTION == JOptionPane
							.showConfirmDialog(
									parentUiComponent,
									"Restoring DB will overwrite currend DB completely \n(all current data will be deleted, and only then new data will be imported).\nDo you wish to backup current DB first?",
									"Backup current DB before restoring new?", JOptionPane.YES_NO_OPTION)) {
						dbOpsMap.get(dbOpsNames[1]).run();
					}
					JFileChooser fileChooser = new JFileChooser();
					if (JFileChooser.APPROVE_OPTION == fileChooser.showOpenDialog(parentUiComponent)) {
						final File fileToLoadFrom = fileChooser.getSelectedFile();
						parentUiComponent.setEnabled(false);
						new Thread() {
							public void run() {
								FileReader fis = null;
								Connection conn = null;
								try {
									// votesFetchController.getStorage().dropAllTables();
									// presDecreesFetchController.getStorage().dropAllTables();
									conn = DataBaseConnectionService.this.getConnection();
									execSingleStatement("DROP ALL OBJECTS");
									fis = new FileReader(fileToLoadFrom);
									RunScript.execute(conn, fis);
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											parentUiComponent.setEnabled(true);

											JOptionPane.showMessageDialog(parentUiComponent, "Script " + fileToLoadFrom.getPath() + " executed successfully",
													"DB restore succeeded", JOptionPane.INFORMATION_MESSAGE);
										}
									});
								} catch (final Exception ex) {
									ex.printStackTrace();
									SwingUtilities.invokeLater(new Runnable() {
										@Override
										public void run() {
											parentUiComponent.setEnabled(true);

											JOptionPane.showMessageDialog(parentUiComponent, ex.getClass().getCanonicalName() + " " + ex.getMessage(),
													"Error occurred", JOptionPane.ERROR_MESSAGE);
										}
									});
								} finally {
									IOUtils.closeSilently(fis);
									JdbcUtils.closeSilently(conn);
								}
							}
						}.start();
					}
				}
			});
			dbOps = Collections.unmodifiableMap(dbOpsMap);
		} else {
			try {
				final Driver mySqlDriver = (Driver) Class.forName("com.mysql.jdbc.Driver").newInstance();
				DriverManager.registerDriver(mySqlDriver);
			} catch (Exception e) {
				throw new RuntimeException(e);
			}
			final StringBuilder jdbcUrl = new StringBuilder("jdbc:mysql://");
			jdbcUrl.append(host).append(":");
			if (port != null) {
				jdbcUrl.append(String.valueOf(port));
			}
			jdbcUrl.append("/").append(dbName).append("?useUnicode=true&characterEncoding=utf8");
			final ComboPooledDataSource c3p0DataSource = new ComboPooledDataSource();
			c3p0DataSource.setUser(login);
			c3p0DataSource.setPassword(password);
			c3p0DataSource.setJdbcUrl(jdbcUrl.toString());
			dataSource = c3p0DataSource;
			disposeOperation = new Runnable() {
				@Override
				public void run() {
					c3p0DataSource.close();
				}
			};
			dbInfo = "MySQL DB " + jdbcUrl.toString();
			dbOpsNames = new String[0];
			dbOps = Collections.emptyMap();
		}
	}

	public Connection getConnection() throws SQLException {
		return dataSource.getConnection();
	}

	public void dispose() {
		this.disposeOperation.run();
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

	public String[] getDbOpsNames() {
		return dbOpsNames;
	}

	public Runnable getDbOp(final String dbOpName) {
		return dbOps.get(dbOpName);
	}

	public String getDbInfo() {
		return dbInfo;
	}
}
