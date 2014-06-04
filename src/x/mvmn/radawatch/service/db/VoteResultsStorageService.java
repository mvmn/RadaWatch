package x.mvmn.radawatch.service.db;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import x.mvmn.radawatch.service.parse.MeetingsListParser.RecordExistenceChecker;
import x.mvmn.radawatch.service.parse.VoteResultsPageDocument;
import x.mvmn.radawatch.service.parse.VoteResultsPageDocument.Faction;
import x.mvmn.radawatch.service.parse.VoteResultsPageDocument.Vote;

public class VoteResultsStorageService implements RecordExistenceChecker {

	protected final StorageService storageService;

	public VoteResultsStorageService(final StorageService storageService) {
		this.storageService = storageService;
	}

	public void save(final VoteResultsPageDocument document) {
		Connection conn = null;
		try {
			conn = storageService.getConnection();
			conn.createStatement().execute("begin transaction");

			final PreparedStatement insertVoteSessionRecord = conn
					.prepareStatement(
							"insert into votesession (g_id, votetitle, votedate, votedyes, votedno, abstained, skipped, total, votepassed) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);

			insertVoteSessionRecord.setInt(1, document.getGlobalId());
			insertVoteSessionRecord.setString(2, document.getTitle());
			insertVoteSessionRecord.setTimestamp(3, new java.sql.Timestamp(document.getDate().getTime()));
			insertVoteSessionRecord.setInt(4, document.getVotedYes());
			insertVoteSessionRecord.setInt(5, document.getVotedNo());
			insertVoteSessionRecord.setInt(6, document.getAbstained());
			insertVoteSessionRecord.setInt(7, document.getSkipped());
			insertVoteSessionRecord.setInt(8, document.getTotal());
			insertVoteSessionRecord.setBoolean(9, document.getResult());

			insertVoteSessionRecord.executeUpdate();
			int voteSessionId;
			{
				ResultSet generatedKeys = insertVoteSessionRecord.getGeneratedKeys();
				generatedKeys.next();
				voteSessionId = generatedKeys.getInt(1);
				generatedKeys.close();
			}

			final PreparedStatement insertVoteFactionRecord = conn
					.prepareStatement(
							"insert into votesessionfaction (votesessionid, title, totalmembers, votedyes, votedno, abstained, skipped, absent) values (?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);
			final PreparedStatement insertVoteRecord = conn
					.prepareStatement("insert into individualvote (votesessionid, votesessionfactionid, name, voted) values (?, ?, ?, ?)");
			for (Faction factionInfo : document.getFactions()) {
				insertVoteFactionRecord.setInt(1, voteSessionId);
				insertVoteFactionRecord.setString(2, factionInfo.getTitle());
				insertVoteFactionRecord.setInt(3, factionInfo.getSize());
				insertVoteFactionRecord.setInt(4, factionInfo.getVotedYes());
				insertVoteFactionRecord.setInt(5, factionInfo.getVotedNo());
				insertVoteFactionRecord.setInt(6, factionInfo.getAbstained());
				insertVoteFactionRecord.setInt(7, factionInfo.getSkipped());
				insertVoteFactionRecord.setInt(8, factionInfo.getAbsent());
				insertVoteFactionRecord.executeUpdate();

				int voteFactionRecordId;
				{
					ResultSet generatedKeys = insertVoteFactionRecord.getGeneratedKeys();
					generatedKeys.next();
					voteFactionRecordId = generatedKeys.getInt(1);
					generatedKeys.close();
				}

				for (Vote voteInfo : factionInfo.getVotes()) {
					insertVoteRecord.setInt(1, voteSessionId);
					insertVoteRecord.setInt(2, voteFactionRecordId);
					insertVoteRecord.setString(3, voteInfo.getName());
					insertVoteRecord.setInt(4, voteInfo.getVote().getId());
					insertVoteRecord.executeUpdate();
				}
			}

			conn.createStatement().execute("commit");
		} catch (Exception e) {
			e.printStackTrace();
			if (conn != null) {
				try {
					conn.createStatement().execute("rollback");
				} catch (Exception tre) {
					tre.printStackTrace();
				}
			}
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception cce) {
				}
			}
		}
	}

	@Override
	public boolean checkExists(int meetingId) {
		boolean result = true;
		Connection conn = null;
		try {
			conn = storageService.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select * from votesession  where g_id = " + meetingId);
			ResultSet rs = stmt.getResultSet();
			result = rs.next();
		} catch (Exception e) {
			e.printStackTrace();
		} finally {
			if (conn != null) {
				try {
					conn.close();
				} catch (Exception cce) {
				}
			}
		}
		return result;
	}

	// public static void main(String args[]) throws Exception {
	// VoteResultsPageDocument testDoc = new VoteResultsPageDocument("http://w1.c1.rada.gov.ua/pls/radan_gs09/ns_golos?g_id=5354");
	// StorageService storService = new StorageService();
	// storService.dropTables();
	// storService.createTables();
	// VoteResultsStorageService test = new VoteResultsStorageService(storService);
	// test.save(testDoc);
	// }
}
