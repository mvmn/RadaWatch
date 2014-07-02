package x.mvmn.radawatch.service.analyze;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.Statement;
import java.sql.Timestamp;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;

import org.h2.util.JdbcUtils;

import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class DeputeesFactionsParticipationAnalyzer {

	public static class DeputyFactionParticipation {
		private final String deputy;
		private final String faction;
		private final Date startDate;
		private final Date endDate;

		public DeputyFactionParticipation(final String deputy, final String faction, final Date startDate, final Date endDate) {
			super();
			this.deputy = deputy;
			this.faction = faction;
			this.startDate = startDate;
			this.endDate = endDate;
		}

		public String getDeputy() {
			return deputy;
		}

		public String getFaction() {
			return faction;
		}

		public Date getStartDate() {
			return startDate;
		}

		public Date getEndDate() {
			return endDate;
		}
	}

	protected final DataBaseConnectionService dbService;

	public DeputeesFactionsParticipationAnalyzer(final DataBaseConnectionService dbService) {
		this.dbService = dbService;
	}

	public List<DeputyFactionParticipation> getData() throws Exception {
		List<DeputyFactionParticipation> result = new ArrayList<DeputyFactionParticipation>();

		Connection conn = null;
		Statement stmt = null;
		try {
			final List<String> deputeesNames = dbService.execSelectOneColumn("select distinct name from individualvote order by name");

			final Date earliestRecordDate = dbService.execSelectOneDate("select min(votedate) from VOTESESSION");
			final Date latestRecordDate = dbService.execSelectOneDate("select max(votedate) from VOTESESSION");
			conn = dbService.getConnection();
			stmt = conn.createStatement();
			for (final String deputyName : deputeesNames) {
				String lastFaction = null;
				Date lastDate = null;
				boolean gotResult;
				do {
					String query = "select min(votedate) as mindate, title from INDIVIDUALVOTE left join VOTESESSIONFACTION on VOTESESSIONFACTION.ID = INDIVIDUALVOTE.VOTESESSIONFACTIONID left join VOTESESSION on VOTESESSION.ID = VOTESESSIONFACTION.VOTESESSIONID where name = ? ";
					if (lastDate != null) {
						query += " AND votedate > ? ";
					}
					if (lastFaction != null) {
						query += " AND NOT(title = ?) ";
					}
					query += " GROUP BY title ";
					final PreparedStatement pstmt = conn.prepareStatement(query);
					stmt = pstmt;
					pstmt.setString(1, deputyName);
					if (lastDate != null) {
						pstmt.setTimestamp(2, new Timestamp(lastDate.getTime()));
					}
					if (lastFaction != null) {
						pstmt.setString(3, lastFaction);
					}
					final ResultSet rs = pstmt.executeQuery();
					if (rs.next()) {
						gotResult = true;
						lastDate = rs.getTimestamp("mindate");
						lastFaction = rs.getString("title");

						final Date endDate;
						final String queryLastDate = "select max(votedate) as maxdate from INDIVIDUALVOTE left join VOTESESSIONFACTION on VOTESESSIONFACTION.ID = INDIVIDUALVOTE.VOTESESSIONFACTIONID left join VOTESESSION on VOTESESSION.ID = VOTESESSIONFACTION.VOTESESSIONID where name = ? AND votedate > ? AND title = ? AND votedate < (select min(votedate) from INDIVIDUALVOTE left join VOTESESSIONFACTION on VOTESESSIONFACTION.ID = INDIVIDUALVOTE.VOTESESSIONFACTIONID left join VOTESESSION on VOTESESSION.ID = VOTESESSIONFACTION.VOTESESSIONID where name = ? AND votedate > ? and not(title = ? ))";
						final PreparedStatement pstmt2 = conn.prepareStatement(queryLastDate);
						pstmt2.setString(1, deputyName);
						pstmt2.setTimestamp(2, new Timestamp(lastDate.getTime()));
						pstmt2.setString(3, lastFaction);
						pstmt2.setString(4, deputyName);
						pstmt2.setTimestamp(5, new Timestamp(lastDate.getTime()));
						pstmt2.setString(6, lastFaction);
						ResultSet rs2 = pstmt2.executeQuery();
						if (rs2.next()) {
							endDate = rs2.getTimestamp(1);
						} else {
							endDate = null;
						}
						rs2.close();
						pstmt2.close();
						result.add(new DeputyFactionParticipation(deputyName, lastFaction, lastDate.after(earliestRecordDate) ? lastDate : null,
								endDate != null && endDate.before(latestRecordDate) ? endDate : null));
					} else {
						gotResult = false;
					}
					rs.close();
					pstmt.close();
				} while (gotResult);
			}
		} finally {
			JdbcUtils.closeSilently(stmt);
			JdbcUtils.closeSilently(conn);
		}

		return result;
	}

}
