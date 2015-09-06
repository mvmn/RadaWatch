package x.mvmn.radawatch.gui.analyze;

import x.mvmn.radawatch.service.db.DataBrowseQuery;

public class ExtFilterPanel extends FilterPanel {
	private static final long serialVersionUID = -7318614206605317327L;

	public ExtFilterPanel(boolean enableDatesFilters, boolean enableSearchPhraseFilter) {
		super(enableDatesFilters, enableSearchPhraseFilter);
	}

	public DataBrowseQuery generateDataBrowseQuery() {
		return generateDataBrowseQuery(null, null);
	}

	public DataBrowseQuery generateDataBrowseQuery(final Integer offset, final Integer count) {
		return new DataBrowseQuery(this.getSearchText(), offset, count, this.getDateFrom(), this.getDateTo());
	}

	public DataBrowseQuery generateDataBrowseQuery(final String overrideSearchText) {
		return generateDataBrowseQuery(overrideSearchText, null, null);
	}

	public DataBrowseQuery generateDataBrowseQuery(final String overrideSearchText, final Integer offset, final Integer count) {
		return new DataBrowseQuery(overrideSearchText, offset, count, this.getDateFrom(), this.getDateTo());
	}
}
