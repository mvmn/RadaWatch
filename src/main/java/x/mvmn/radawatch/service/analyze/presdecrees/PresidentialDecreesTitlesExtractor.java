package x.mvmn.radawatch.service.analyze.presdecrees;

import x.mvmn.radawatch.service.analyze.AbstractTitlesExtractor;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class PresidentialDecreesTitlesExtractor extends AbstractTitlesExtractor {

	public PresidentialDecreesTitlesExtractor(final DataBaseConnectionService storageService) {
		super(storageService);
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
	protected String getQueryColumns() {
		return " concat(decreetype,': ', title, '. /', reldate,'/ ', numcode) ";
	}
}
