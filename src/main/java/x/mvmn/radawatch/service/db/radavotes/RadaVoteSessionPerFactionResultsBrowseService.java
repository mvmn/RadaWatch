package x.mvmn.radawatch.service.db.radavotes;

import java.sql.ResultSet;

import x.mvmn.radawatch.gui.browse.DataBrowser.ViewAdaptor;
import x.mvmn.radawatch.model.radavotes.VoteSessionPerFactionData;
import x.mvmn.radawatch.service.db.AbstractDataBrowseService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaVoteSessionPerFactionResultsBrowseService extends AbstractDataBrowseService<VoteSessionPerFactionData> {

	public RadaVoteSessionPerFactionResultsBrowseService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	@Override
	protected String getTableName() {
		return " votesessionfaction ";
	}

	@Override
	protected String getTitleColumnName() {
		return " title ";
	}

	@Override
	protected String getDateColumnName() {
		return null;
	}

	@Override
	protected String getShortColumnsList() {
		return " id, votesessionid, title, totalmembers, votedyes, votedno, abstained, skipped, absent ";
	}

	@Override
	protected VoteSessionPerFactionData resultSetRowToEntity(ResultSet resultSet) throws Exception {
		final int dbId = resultSet.getInt("id");
		final String title = resultSet.getString("title");
		final int size = resultSet.getInt("totalmembers");
		final int votedYes = resultSet.getInt("votedyes");
		final int votedNo = resultSet.getInt("votedno");
		final int abstained = resultSet.getInt("abstained");
		final int skipped = resultSet.getInt("skipped");
		final int absent = resultSet.getInt("absent");

		return new VoteSessionPerFactionData(dbId, title, size, votedYes, votedNo, abstained, skipped, absent, null);
	}

	public static class RadaVotesPerFactionViewAdaptor implements ViewAdaptor<VoteSessionPerFactionData> {

		@Override
		public int getFieldsCount(boolean fullView) {
			return 8;
		}

		private static final String[] COLUMN_NAMES = new String[] { "DB ID", "Title", "Total", "Yes votes", "No votes", "Abstained", "Skipped", "Absent" };

		@Override
		public String getFieldName(int fieldIndex, boolean fullView) {
			return COLUMN_NAMES[fieldIndex];
		}

		@Override
		public String getFieldValue(VoteSessionPerFactionData item, int fieldIndex, boolean fullView) {
			final String result;
			switch (fieldIndex) {
				case 0:
					result = String.valueOf(item.getDbId());
				break;
				case 1:
					result = item.getTitle();
				break;
				case 2:
					result = String.valueOf(item.getSize());
				break;
				case 3:
					result = String.valueOf(item.getVotedYes());
				break;
				case 4:
					result = String.valueOf(item.getVotedNo());
				break;
				case 5:
					result = String.valueOf(item.getAbstained());
				break;
				case 6:
					result = String.valueOf(item.getSkipped());
				break;
				case 7:
					result = String.valueOf(item.getAbsent());
				break;
				default:
					result = item.getTitle();
			}
			return result;
		}
	}

	@Override
	protected String getParentIdColumnName() {
		return " votesessionid ";
	}
}