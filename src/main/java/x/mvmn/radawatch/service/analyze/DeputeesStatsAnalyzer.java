package x.mvmn.radawatch.service.analyze;

import java.sql.Connection;
import java.sql.ResultSet;
import java.sql.Statement;
import java.util.ArrayList;
import java.util.List;

import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class DeputeesStatsAnalyzer {

	public static class DeputyStats {

		private final String name;
		private final int totalVotingRecords;
		private final int votedFor;
		private final int votedAgainst;
		private final int voteAbstained;
		private final int votesSkipped;
		private final int votesAbsent;
		private final int totalFactions;

		public DeputyStats(String name, int totalVotingRecords, int votedFor, int votedAgainst, int voteAbstained, int votesSkipped, int votesAbsent,
				int totalFactions) {
			super();
			this.name = name;
			this.totalVotingRecords = totalVotingRecords;
			this.votedFor = votedFor;
			this.votedAgainst = votedAgainst;
			this.voteAbstained = voteAbstained;
			this.votesSkipped = votesSkipped;
			this.votesAbsent = votesAbsent;
			this.totalFactions = totalFactions;
		}

		public String getName() {
			return name;
		}

		public int getTotalVotingRecords() {
			return totalVotingRecords;
		}

		public int getVotedFor() {
			return votedFor;
		}

		public int getVotedAgainst() {
			return votedAgainst;
		}

		public int getVoteAbstained() {
			return voteAbstained;
		}

		public int getVotesSkipped() {
			return votesSkipped;
		}

		public int getVotesAbsent() {
			return votesAbsent;
		}

		public int getTotalFactions() {
			return totalFactions;
		}
	}

	private static final String SQL = "select name, count(*) as totalvotings, sum(voted=1) as votedfor, sum(voted=2) as votedagainst, sum(voted=3) as abstained, sum(voted=4) as skipped, sum(voted=5) as absent, count(distinct VOTESESSIONFACTION.title) as totalfactions from INDIVIDUALVOTE  left join VOTESESSIONFACTION on VOTESESSIONFACTION.ID = INDIVIDUALVOTE.VOTESESSIONFACTIONID group by name";

	protected final DataBaseConnectionService dbService;

	public DeputeesStatsAnalyzer(final DataBaseConnectionService dbService) {
		this.dbService = dbService;
	}

	public List<DeputyStats> getStats() throws Exception {
		List<DeputyStats> results;

		Connection conn = null;
		Statement stmt = null;
		try {
			conn = dbService.getConnection();
			stmt = conn.createStatement();
			final ResultSet resultSet = stmt.executeQuery(SQL);
			results = new ArrayList<DeputyStats>();
			while (resultSet.next()) {
				final String name = resultSet.getString("name");
				final int totalVotingRecords = resultSet.getInt("totalvotings");
				final int votedFor = resultSet.getInt("votedfor");
				final int votedAgainst = resultSet.getInt("votedagainst");
				final int voteAbstained = resultSet.getInt("abstained");
				final int votesSkipped = resultSet.getInt("skipped");
				final int votesAbsent = resultSet.getInt("absent");
				final int totalFactions = resultSet.getInt("totalfactions");

				results.add(new DeputyStats(name, totalVotingRecords, votedFor, votedAgainst, voteAbstained, votesSkipped, votesAbsent, totalFactions));
			}
		} finally {
			JdbcUtils.closeSilently(stmt);
			JdbcUtils.closeSilently(conn);
		}

		return results;
	}
}
