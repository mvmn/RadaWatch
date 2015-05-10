package x.mvmn.radawatch.service.db.radavotes;

import java.sql.ResultSet;

import x.mvmn.radawatch.gui.browse.DataBrowser.ViewAdaptor;
import x.mvmn.radawatch.model.radavotes.IndividualDeputyVoteData;
import x.mvmn.radawatch.service.db.AbstractDataBrowseService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaIndividualVotesBrowseService extends AbstractDataBrowseService<IndividualDeputyVoteData> {

	private static String[] SEARCH_PHRASE_COLUMNS = new String[] { " name " };

	public RadaIndividualVotesBrowseService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	@Override
	protected String getTableName() {
		return " individualvote ";
	}

	@Override
	protected String getParentIdColumnName() {
		return " votesessionfactionid ";
	}

	@Override
	protected String[] getSearchPhraseColumnNames() {
		return SEARCH_PHRASE_COLUMNS;
	}

	@Override
	protected String getDateColumnName() {
		return null;
	}

	@Override
	protected String getShortColumnsList() {
		return "id, name, voted";
	}

	@Override
	protected IndividualDeputyVoteData resultSetRowToEntity(ResultSet resultSet) throws Exception {
		final int dbId = resultSet.getInt("id");
		final String name = resultSet.getString("name");
		final String vote = resultSet.getString("voted");
		return new IndividualDeputyVoteData(dbId, name, vote);
	}

	public static class IndividualDeputyVoteViewAdaptor implements ViewAdaptor<IndividualDeputyVoteData> {

		@Override
		public int getFieldsCount(boolean fullView) {
			return 3;
		}

		private static final String[] FIELD_NAMES = new String[] { "DB ID", "Name", "Vote" };
		private static final Class<?>[] FIELD_TYPES = new Class<?>[] { Integer.class, String.class, String.class };

		@Override
		public String getFieldName(int fieldIndex, boolean fullView) {
			return FIELD_NAMES[fieldIndex];
		}

		@Override
		public Object getFieldValue(IndividualDeputyVoteData item, int fieldIndex, boolean fullView) {
			Object result;
			switch (fieldIndex) {
				case 0:
					result = item.getDbId();
				break;
				case 1:
					result = item.getName();
				break;
				case 2:
					result = item.getVote().name();
				break;
				default:
					result = item.getName();
			}
			return result;
		}

		@Override
		public Class<?> getFieldType(int fieldIndex, boolean fullView) {
			return FIELD_TYPES[fieldIndex];
		}
	}

}
