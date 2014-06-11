package x.mvmn.radawatch.service.db.radavotes;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;

import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.model.radavotes.DeputyVoteData;
import x.mvmn.radawatch.model.radavotes.VoteFactionData;
import x.mvmn.radawatch.model.radavotes.VoteResultsData;
import x.mvmn.radawatch.service.db.AbstractDataStorageService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaVotesStorageService extends AbstractDataStorageService<VoteResultsData> {

	private static final String[] SQL_TABLES = new String[] { "votesession", "votesessionfaction", "individualvote" };
	private static final String SQL_TABLES_DEFINITIONS[] = new String[] {
			"id int not null primary key auto_increment, g_id int not null, votetitle varchar(16384), votedate TIMESTAMP, votedyes int, votedno int, abstained int, skipped int, total int, votepassed BOOL",
			"id int not null primary key auto_increment, votesessionid int, title varchar(16384), totalmembers int, votedyes int, votedno int, abstained int, skipped int, absent int",
			"id int not null primary key auto_increment, votesessionid int, votesessionfactionid int, name varchar(16384), voted varchar(1024)" };

	public RadaVotesStorageService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	public void storeNewRecord(final VoteResultsData data) throws Exception {
		Connection conn = null;
		try {
			conn = dbService.getConnection();
			conn.createStatement().execute("begin transaction");

			final PreparedStatement insertVoteSessionRecord = conn
					.prepareStatement(
							"insert into votesession (g_id, votetitle, votedate, votedyes, votedno, abstained, skipped, total, votepassed) values (?, ?, ?, ?, ?, ?, ?, ?, ?)",
							Statement.RETURN_GENERATED_KEYS);

			insertVoteSessionRecord.setInt(1, data.getGlobalId());
			insertVoteSessionRecord.setString(2, data.getTitle());
			insertVoteSessionRecord.setTimestamp(3, new java.sql.Timestamp(data.getDate().getTime()));
			insertVoteSessionRecord.setInt(4, data.getVotedYes());
			insertVoteSessionRecord.setInt(5, data.getVotedNo());
			insertVoteSessionRecord.setInt(6, data.getAbstained());
			insertVoteSessionRecord.setInt(7, data.getSkipped());
			insertVoteSessionRecord.setInt(8, data.getTotal());
			insertVoteSessionRecord.setBoolean(9, data.getResult());

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
			for (VoteFactionData factionInfo : data.getFactions()) {
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

				for (DeputyVoteData voteInfo : factionInfo.getVotes()) {
					insertVoteRecord.setInt(1, voteSessionId);
					insertVoteRecord.setInt(2, voteFactionRecordId);
					insertVoteRecord.setString(3, voteInfo.getName());
					insertVoteRecord.setInt(4, voteInfo.getVote().getId());
					insertVoteRecord.executeUpdate();
				}
			}

			conn.createStatement().execute("commit");
		} catch (Exception e) {
			if (conn != null) {
				try {
					conn.createStatement().execute("rollback");
				} catch (Exception tre) {
					tre.printStackTrace();
				}
			}
			throw e;
		} finally {
			JdbcUtils.closeSilently(conn);
		}
	}

	// public List<VoteResultsData> getVoteResults(final Integer offset, final Integer count) {
	// List<VoteResultsData> result = null;
	// // TODO: implement
	//
	// return result;
	// }

	@Override
	public boolean checkExists(int meetingId) throws Exception {
		boolean result = true;
		Connection conn = null;
		try {
			conn = dbService.getConnection();
			Statement stmt = conn.createStatement();
			stmt.execute("select * from votesession  where g_id = " + meetingId);
			ResultSet rs = stmt.getResultSet();
			result = rs.next();
		} finally {
			JdbcUtils.closeSilently(conn);
		}
		return result;
	}

	@Override
	public String[] getTablesNames() {
		return SQL_TABLES;
	}

	@Override
	public String getTableDefinitionSql(final String tableName) {
		int index = -1;
		for (int i = 0; i < SQL_TABLES.length; i++) {
			if (SQL_TABLES[i].equalsIgnoreCase(tableName)) {
				index = i;
				break;
			}
		}
		return SQL_TABLES_DEFINITIONS[index];
	}
}
