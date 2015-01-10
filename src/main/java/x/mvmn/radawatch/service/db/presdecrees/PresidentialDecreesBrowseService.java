package x.mvmn.radawatch.service.db.presdecrees;

import java.sql.Date;
import java.sql.ResultSet;

import x.mvmn.radawatch.gui.browse.DataBrowser.ViewAdaptor;
import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.service.db.AbstractDataBrowseService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesBrowseService extends AbstractDataBrowseService<PresidentialDecree> {

	private static final String[] COLUMN_NAMES = new String[] { "DB ID", "Site ID", "Date", "Numcode", "Type", "Title", "Full text" };
	private static final Class<?>[] COLUMN_TYPES = new Class<?>[] { Integer.class, Integer.class, Date.class, String.class, String.class, String.class,
			String.class };

	public static class PresidentialDecreesViewAdaptor implements ViewAdaptor<PresidentialDecree> {

		@Override
		public int getFieldsCount(final boolean fullView) {
			return fullView ? 7 : 6;
		}

		@Override
		public String getFieldName(int fieldIndex, final boolean fullView) {
			return COLUMN_NAMES[fieldIndex];
		}

		@Override
		public Object getFieldValue(PresidentialDecree item, int fieldIndex, final boolean fullView) {
			final Object result;
			switch (fieldIndex) {
				case 0:
					result = item.getDbId();
				break;
				case 1:
					result = item.getSiteId();
				break;
				case 2:
					result = item.getDate();
				break;
				case 3:
					result = item.getNumberCode();
				break;
				case 4:
					result = item.getType();
				break;
				case 5:
					result = item.getTitle();
				break;
				case 6:
					result = item.getFullText();
				break;
				default:
					result = item.getTitle();
			}

			return result;
		}

		@Override
		public Class<?> getFieldType(int fieldIndex, boolean fullView) {
			return COLUMN_TYPES[fieldIndex];
		}
	};

	public PresidentialDecreesBrowseService(final DataBaseConnectionService dbService) {
		super(dbService);
	}

	@Override
	protected String getTableName() {
		return " presidentialdecree ";
	}

	@Override
	protected String getTitleColumnName() {
		return " title ";
	}

	@Override
	protected String getDateColumnName() {
		return " reldate ";
	}

	@Override
	protected String getShortColumnsList() {
		return " id, g_id, reldate, decreetype, numcode, title ";
	}

	@Override
	protected PresidentialDecree resultSetRowToEntity(final ResultSet resultSet) throws Exception {
		final int dbId = resultSet.getInt("id");
		final int siteId = resultSet.getInt("g_id");
		final Date date = resultSet.getDate("reldate");
		final String title = resultSet.getString("title");
		final String decreeType = resultSet.getString("decreetype");
		final String numCode = resultSet.getString("numcode");
		final String fullText;
		if (resultSet.getMetaData().getColumnCount() > 6) {
			fullText = resultSet.getString("completetext");
		} else {
			fullText = null;
		}
		return new PresidentialDecree(dbId, siteId, decreeType, title, date, numCode, fullText);
	}

	@Override
	protected String getParentIdColumnName() {
		return null;
	}
}
