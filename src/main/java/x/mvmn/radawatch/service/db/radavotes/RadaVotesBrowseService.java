package x.mvmn.radawatch.service.db.radavotes;

import java.sql.Date;
import java.sql.ResultSet;

import x.mvmn.radawatch.gui.browse.DataBrowser.ViewAdaptor;
import x.mvmn.radawatch.model.radavotes.VoteSessionResultsData;
import x.mvmn.radawatch.service.db.AbstractDataBrowseService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaVotesBrowseService extends AbstractDataBrowseService<VoteSessionResultsData> {

	public RadaVotesBrowseService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	@Override
	protected String getTableName() {
		return " votesession ";
	}

	@Override
	protected String getTitleColumnName() {
		return " votetitle ";
	}

	@Override
	protected String getDateColumnName() {
		return " votedate ";
	}

	@Override
	protected String getShortColumnsList() {
		return "id, g_id, votetitle, votedate, votedyes, votedno, abstained, skipped, total, votepassed ";
	}

	@Override
	protected VoteSessionResultsData resultSetRowToEntity(ResultSet resultSet) throws Exception {
		final int dbId = resultSet.getInt("id");
		final int siteId = resultSet.getInt("g_id");
		final String title = resultSet.getString("votetitle");
		final boolean result = resultSet.getBoolean("votepassed");
		final Date date = resultSet.getDate("votedate");
		final int votedYes = resultSet.getInt("votedyes");
		final int votedNo = resultSet.getInt("votedno");
		final int abstained = resultSet.getInt("abstained");
		final int skipped = resultSet.getInt("skipped");
		final int total = resultSet.getInt("total");

		return new VoteSessionResultsData(dbId, siteId, title, result, date, votedYes, votedNo, abstained, skipped, total, null);
	}

	public static class RadaVotesViewAdaptor implements ViewAdaptor<VoteSessionResultsData> {

		@Override
		public int getFieldsCount(boolean fullView) {
			return 10;
		}

		private static final String[] COLUMN_NAMES = new String[] { "DB ID", "Site ID", "Title", "Passed", "Date", "Yes votes", "No votes", "Abstained",
				"Skipped", "Total" };

		@Override
		public String getFieldName(int fieldIndex, boolean fullView) {
			return COLUMN_NAMES[fieldIndex];
		}

		@Override
		public String getFieldValue(VoteSessionResultsData item, int fieldIndex, boolean fullView) {
			String result;
			switch (fieldIndex) {
				case 0:
					result = String.valueOf(item.getDbId());
				break;
				case 1:
					result = String.valueOf(item.getGlobalId());
				break;
				case 2:
					result = item.getTitle();
				break;
				case 3:
					result = item.getResult().booleanValue() ? "Passed" : "Failed";
				break;
				case 4:
					result = item.getDate().toString();
				break;
				case 5:
					result = String.valueOf(item.getVotedYes());
				break;
				case 6:
					result = String.valueOf(item.getVotedNo());
				break;
				case 7:
					result = String.valueOf(item.getAbstained());
				break;
				case 8:
					result = String.valueOf(item.getSkipped());
				break;
				case 9:
					result = String.valueOf(item.getTotal());
				break;
				default:
					result = item.getTitle();
			}
			return result;
		}

	}
}
