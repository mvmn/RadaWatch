package x.mvmn.radawatch.service.db;

import java.sql.Connection;
import java.sql.Statement;

import x.mvmn.radawatch.service.parse.VoteResultsPageDocument;

public class VoteResultsStorageService {

	protected final StorageService storageService;

	public VoteResultsStorageService(final StorageService storageService) {
		this.storageService = storageService;
	}

	public void save(final VoteResultsPageDocument document) {
		Connection conn = null;
		try {
			conn = storageService.getConnection();
			conn.createStatement().execute("begin transaction");

			Statement update = conn.prepareStatement("");

			update.executeUpdate("", Statement.RETURN_GENERATED_KEYS);
			update.getGeneratedKeys();

			conn.createStatement().execute("commit");
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.createStatement().execute("rollback");
				} catch (Exception tre) {
					tre.printStackTrace();
				}
				try {
					conn.close();
				} catch (Exception cce) {
				}
			}
		}
	}
}
