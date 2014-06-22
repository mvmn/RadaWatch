package x.mvmn.radawatch.service.analyze.radavotes;

import x.mvmn.radawatch.service.analyze.AbstractTitlesExtractor;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;

public class RadaVotesTitlesExtractor extends AbstractTitlesExtractor {

	public RadaVotesTitlesExtractor(final DataBaseConnectionService storageService) {
		super(storageService);
	}

	@Override
	protected String getQueryColumns() {
		return " concat(votetitle, '. /', votedate,'/', case votepassed when true then ' <прийнято>' else ' <НЕ прийнято>' end case, ' ', votedyes,'/',votedno,' (',total,')') ";
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
}
