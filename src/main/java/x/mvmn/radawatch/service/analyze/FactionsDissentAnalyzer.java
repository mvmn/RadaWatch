package x.mvmn.radawatch.service.analyze;

import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.DataBrowseQuery;
import x.mvmn.radawatch.service.db.ResultSetConverter;

public class FactionsDissentAnalyzer<T> extends AbstractDissentAnalyzer<T> {
	protected final String FACTIONS_TITLE_COLUMNS[] = { "title" };
	protected final String FACTIONS_OR_VOTE_TITLE_COLUMNS[] = { "title", "votetitle" };

	public FactionsDissentAnalyzer(DataBaseConnectionService dbService, ResultSetConverter<T> converter) {
		super(dbService, converter);
	}

	public T queryForFactionsDissent(final boolean excludeSkipped, final boolean excludeAbsent, final DataBrowseQuery query) throws Exception {
		return processQuery(buildQueryForFactionsDissent(excludeSkipped, excludeAbsent, query), converter);
	}

	public T queryForDissentingLaws(final int threshold, final boolean excludeSkipped, final boolean excludeAbsent, final DataBrowseQuery query)
			throws Exception {
		return processQuery(buildQueryForDissentingLaws(threshold, excludeSkipped, excludeAbsent, query), converter);
	}

	public String buildQueryForFactionsDissent(final boolean excludeSkipped, final boolean excludeAbsent, final DataBrowseQuery filter) {
		return new StringBuilder(
				"select faction,  min(percentage) as minimal, max(percentage) as maximal, avg(percentage) as average_consent_percentage, std(percentage) as deviation ")
				.append(" from (select title as faction, ").append(" case when (votesessionfaction.totalmembers-votesessionfaction.absent)>0 THEN ")
				.append(" (abs(votesessionfaction.votedyes - votesessionfaction.votedno - votesessionfaction.abstained")
				.append(!excludeSkipped ? " - votesessionfaction.skipped" : "").append(!excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(")*100)/(votesessionfaction.totalmembers").append(excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(excludeSkipped ? " - votesessionfaction.skipped" : "").append(") ").append(" ELSE 100 END as percentage ")
				.append(" from votesessionfaction left join votesession on votesession.id = votesessionfaction.votesessionid ")
				.append(filter.generateWhereClause(FACTIONS_TITLE_COLUMNS, "votedate"))
				.append(" ) as tmpt group by faction order by average_consent_percentage desc").toString();
	}

	public String buildQueryForDissentingLaws(final int threshold, final boolean excludeSkipped, final boolean excludeAbsent, final DataBrowseQuery filter) {
		return new StringBuilder("select votesession.id as voteid, votesession.g_id as siteid, title as faction, votetitle, ")
				.append("case when (votesessionfaction.totalmembers-votesessionfaction.absent)>0 THEN (abs(")
				.append("votesessionfaction.votedyes - votesessionfaction.votedno - votesessionfaction.abstained")
				.append(!excludeSkipped ? " - votesessionfaction.skipped" : "").append(!excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(")*100)/(votesessionfaction.totalmembers").append(excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(excludeSkipped ? " - votesessionfaction.skipped" : "").append(") ").append(" ELSE 100 END as consent_percentage ")
				.append(" from votesessionfaction left join votesession on votesession.id = votesessionfaction.votesessionid ")
				.append(filter.generateWhereClause(FACTIONS_OR_VOTE_TITLE_COLUMNS, "votedate")).append("having consent_percentage <= ")
				.append(String.valueOf(threshold)).append(" order by faction, consent_percentage asc, votetitle").toString();
	}
}
