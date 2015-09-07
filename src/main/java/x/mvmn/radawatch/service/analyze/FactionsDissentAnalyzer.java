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

	public T queryForFactionsDissent(final boolean excludeSkipped, final boolean excludeAbsent, final boolean normalizeByEffectiveVotes,
			final DataBrowseQuery query) throws Exception {
		return processQuery(buildQueryForFactionsDissent(excludeSkipped, excludeAbsent, normalizeByEffectiveVotes, query), converter);
	}

	public T queryForDissentingLaws(final int threshold, final boolean excludeSkipped, final boolean excludeAbsent, final boolean normalizeByEffectiveVotes,
			final DataBrowseQuery query) throws Exception {
		return processQuery(buildQueryForDissentingLaws(threshold, excludeSkipped, excludeAbsent, normalizeByEffectiveVotes, query), converter);
	}

	public String buildQueryForFactionsDissent(final boolean excludeSkipped, final boolean excludeAbsent, final boolean normalizeByEffectiveVotes,
			final DataBrowseQuery filter) {
		final StringBuilder result = new StringBuilder(
				"select faction,  min(percentage) as minimal, max(percentage) as maximal, avg(percentage) as average_dissent_percentage, std(percentage) as deviation ")
				.append(" from (select title as faction, ").append(" case when (votesessionfaction.totalmembers")
				.append(excludeAbsent ? " - votesessionfaction.absent" : "").append(excludeSkipped ? " - votesessionfaction.skipped" : "")
				.append(")>0 THEN (100 - (abs(votesessionfaction.votedyes - votesessionfaction.votedno - votesessionfaction.abstained")
				.append(!excludeSkipped ? " - votesessionfaction.skipped" : "").append(!excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(")*100)/(votesessionfaction.totalmembers").append(excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(excludeSkipped ? " - votesessionfaction.skipped" : "").append(")) ");

		if (normalizeByEffectiveVotes) {
			result.append("* ((votesessionfaction.votedyes + votesessionfaction.votedno + votesessionfaction.abstained ");
			if (!excludeSkipped) {
				result.append(" + votesessionfaction.skipped ");
			}
			if (!excludeAbsent) {
				result.append(" + votesessionfaction.absent ");
			}
			result.append(")/votesessionfaction.totalmembers)");
		}

		result.append(" ELSE 0 END as percentage ")
				.append(" from votesessionfaction left join votesession on votesession.id = votesessionfaction.votesessionid ")
				.append(filter.generateWhereClause(FACTIONS_TITLE_COLUMNS, "votedate"))
				.append(" ) as tmpt group by faction order by average_dissent_percentage desc");
		return result.toString();
	}

	public String buildQueryForDissentingLaws(final int threshold, final boolean excludeSkipped, final boolean excludeAbsent,
			final boolean normalizeByEffectiveVotes, final DataBrowseQuery filter) {

		final String denominator = new StringBuilder("votesessionfaction.totalmembers").append(excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(excludeSkipped ? " - votesessionfaction.skipped" : "").toString();

		final StringBuilder result = new StringBuilder("select votesession.id, title as faction, votesession.votedate, votetitle, ")
				.append("case when (votesessionfaction.totalmembers").append(excludeAbsent ? " - votesessionfaction.absent" : "")
				.append(excludeSkipped ? " - votesessionfaction.skipped" : "").append(")>0 THEN (100 - (abs(")
				.append("votesessionfaction.votedyes - votesessionfaction.votedno - votesessionfaction.abstained")
				.append(!excludeSkipped ? " - votesessionfaction.skipped" : "").append(!excludeAbsent ? " - votesessionfaction.absent" : "").append(")*100)/(")
				.append(denominator).append(")) ");

		if (normalizeByEffectiveVotes) {
			result.append("* ((votesessionfaction.votedyes + votesessionfaction.votedno + votesessionfaction.abstained ");
			if (!excludeSkipped) {
				result.append(" + votesessionfaction.skipped ");
			}
			if (!excludeAbsent) {
				result.append(" + votesessionfaction.absent ");
			}
			result.append(")/votesessionfaction.totalmembers)");
		}

		result.append(
				" ELSE 0 END as dissent_percentage, votesessionfaction.votedyes, votesessionfaction.votedno, votesessionfaction.abstained, votesessionfaction.skipped, votesessionfaction.absent ")
				.append(" from votesessionfaction left join votesession on votesession.id = votesessionfaction.votesessionid ")
				.append(filter.generateWhereClause(FACTIONS_OR_VOTE_TITLE_COLUMNS, "votedate")).append("having dissent_percentage >= ")
				.append(String.valueOf(threshold)).append(" order by faction, dissent_percentage asc, votedate");
		return result.toString();
	}
}
