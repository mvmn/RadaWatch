package x.mvmn.radawatch.service.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
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

			PreparedStatement update = conn
					.prepareStatement(
							"insert into votesession (g_id, votetitle, votedate, votedyes, votedno, abstained, skipped, total, votepassed) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);

			update.setInt(1, document.getGlobalId());
			update.setString(2, document.getTitle());
			update.setTimestamp(3, new java.sql.Timestamp(document.getDate().getTime()));
			update.setInt(4, document.getVotedYes());
			update.setInt(5, document.getVotedNo());
			update.setInt(6, document.getAbstained());
			update.setInt(7, document.getSkipped());
			update.setInt(8, document.getTotal());
			update.setBoolean(9, document.getResult());

			update.executeUpdate();
			update.getGeneratedKeys();

			conn.createStatement().execute("commit");
		} catch (Exception e) {
			e.printStackTrace();
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

	public static void main(String args[]) throws Exception {
		VoteResultsPageDocument testDoc = new VoteResultsPageDocument("http://w1.c1.rada.gov.ua/pls/radan_gs09/ns_golos?g_id=5354");
		StorageService storService = new StorageService();
		storService.dropTables();
		storService.createTables();
		VoteResultsStorageService test = new VoteResultsStorageService(storService);
		test.save(testDoc);
	}
}
