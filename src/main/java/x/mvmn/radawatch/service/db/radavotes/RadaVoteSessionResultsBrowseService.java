package x.mvmn.radawatch.service.db.radavotes;

import java.sql.ResultSet;
import java.sql.Timestamp;
import java.text.SimpleDateFormat;

import x.mvmn.radawatch.gui.browse.DataBrowser.ViewAdaptor;
import x.mvmn.radawatch.model.radavotes.VoteSessionResultsData;
import x.mvmn.radawatch.service.db.AbstractDataBrowseService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaVoteSessionResultsBrowseService extends AbstractDataBrowseService<VoteSessionResultsData> {

	public RadaVoteSessionResultsBrowseService(final DataBaseConnectionService dbService) {
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
		final Timestamp date = resultSet.getTimestamp("votedate");
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
		public Object getFieldValue(VoteSessionResultsData item, int fieldIndex, boolean fullView) {
			Object result;
			switch (fieldIndex) {
				case 0:
					result = item.getDbId();
				break;
				case 1:
					result = item.getGlobalId();
				break;
				case 2:
					result = item.getTitle();
				break;
				case 3:
					result = item.getResult().booleanValue() ? "Passed" : "Failed";
				break;
				case 4:
					result = new SimpleDateFormat("yyyy-MM-dd HH:mm").format(item.getDate());
				break;
				case 5:
					result = item.getVotedYes();
				break;
				case 6:
					result = item.getVotedNo();
				break;
				case 7:
					result = item.getAbstained();
				break;
				case 8:
					result = item.getSkipped();
				break;
				case 9:
					result = item.getTotal();
				break;
				default:
					result = item.getTitle();
			}
			return result;
		}

		@Override
		public Class<?> getFieldType(int fieldIndex, boolean fullView) {
			if (fieldIndex > 1 && fieldIndex < 5) {
				return String.class;
			} else {
				return Integer.class;
			}
		}

	}

	@Override
	protected String getParentIdColumnName() {
		return null;
	}
}
