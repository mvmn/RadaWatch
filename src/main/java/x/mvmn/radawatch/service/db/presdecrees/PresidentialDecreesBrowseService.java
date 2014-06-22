package x.mvmn.radawatch.service.db.presdecrees;

import java.sql.Date;
import java.sql.ResultSet;

import x.mvmn.radawatch.gui.browse.DataBrowser.ViewAdaptor;
import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.service.db.AbstractDataBrowseService;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesBrowseService extends AbstractDataBrowseService<PresidentialDecree> {

	public static class PresidentialDecreesViewAdaptor implements ViewAdaptor<PresidentialDecree> {

		@Override
		public int getFieldsCount(final boolean fullView) {
			return fullView ? 7 : 6;
		}

		@Override
		public String getFieldName(int fieldIndex, final boolean fullView) {
			final String result;
			switch (fieldIndex) {
				case 0:
					result = "DB ID";
				break;
				case 1:
					result = "Site ID";
				break;
				case 2:
					result = "Date";
				break;
				case 3:
					result = "Numcode";
				break;
				case 4:
					result = "Type";
				break;
				case 5:
					result = "Title";
				break;
				case 6:
					result = "Full text";
				break;
				default:
					result = "Title";
			}
			return result;
		}

		@Override
		public String getFieldValue(PresidentialDecree item, int fieldIndex, final boolean fullView) {
			final String result;
			switch (fieldIndex) {
				case 0:
					result = String.valueOf(item.getDbId());
				break;
				case 1:
					result = String.valueOf(item.getSiteId());
				break;
				case 2:
					result = item.getDate().toString();
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
			fullText = resultSet.getString("fulltext");
		} else {
			fullText = null;
		}
		return new PresidentialDecree(dbId, siteId, decreeType, title, date, numCode, fullText);
	}
}
