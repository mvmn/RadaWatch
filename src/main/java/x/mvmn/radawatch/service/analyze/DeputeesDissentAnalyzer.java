package x.mvmn.radawatch.service.analyze;

import x.mvmn.radawatch.model.radavotes.IndividualDeputyVoteData.VoteType;
import x.mvmn.radawatch.service.db.AbstractQueryProcessor;
import x.mvmn.radawatch.service.db.DataBaseConnectionService;
import x.mvmn.radawatch.service.db.DataBrowseQuery;
import x.mvmn.radawatch.service.db.ResultSetConverter;

public class DeputeesDissentAnalyzer<T> extends AbstractQueryProcessor {
	protected final String TITLE_COLUMNS[] = { "title", "name" };

	protected final DataBaseConnectionService dbService;
	protected final ResultSetConverter<T> converter;

	public DeputeesDissentAnalyzer(final DataBaseConnectionService dbService, final ResultSetConverter<T> converter) {
		super();
		this.dbService = dbService;
		this.converter = converter;
	}

	public T queryForDeputeesByFactionsDissent(final int percent, final DataBrowseQuery query) throws Exception {
		return processQuery(buildSqlQueryForDeputeesByFactionsDissent(percent, query), converter);
	}

	public T queryForDeputeeDissentingLaws(final int percent, final DataBrowseQuery query) throws Exception {
		return processQuery(buildSqlQueryForDeputeeDissentingLaws(percent, query), converter);
	}

	public String buildSqlQueryForDeputeesByFactionsDissent(final int percent, final DataBrowseQuery query) {
		return new StringBuilder(
				"select totalvotes as TotalVotes, title as Faction, name as Deputee, groupingvote as ConsentingVotes, notdeputee as DepDissent, absent as DepAbsence, notdeputee-absent as DepDissentSansAbsence, ((notdeputee-absent)*100)/groupingvote as DDSAPercentage from (")
				.append("select count(*) as totalvotes, title, name, ")
				.append(" sum(CASE WHEN ((votesessionfaction.votedyes*100)/(votesessionfaction.totalmembers-votesessionfaction.absent)>")
				.append(String.valueOf(percent)).append(") THEN 1 ELSE 0 END) as groupingvote, ")
				.append(" sum(CASE WHEN ((votesessionfaction.votedyes*100)/(votesessionfaction.totalmembers-votesessionfaction.absent)>")
				.append(String.valueOf(percent)).append(" and not individualvote.voted = 1) THEN 1 ELSE 0 END) as notdeputee, ")
				.append(" sum(CASE WHEN ((votesessionfaction.votedyes*100)/(votesessionfaction.totalmembers-votesessionfaction.absent)>")
				.append(String.valueOf(percent)).append(" and individualvote.voted = 5) THEN 1 ELSE 0 END) as absent from votesession ")
				.append(" left join votesessionfaction on votesessionfaction.votesessionid = votesession.id ")
				.append(" left join individualvote on individualvote.votesessionfactionid = votesessionfaction.id ")
				.append(query.generateWhereClause(TITLE_COLUMNS, "votedate", true))
				.append(" group by name, title) as tmpt order by title, DDSAPercentage desc").toString();
	}

	public String buildSqlQueryForDeputeeDissentingLaws(final int percent, final DataBrowseQuery query) {
		return new StringBuilder("select title as Faction, voted as VoteType, votedate as Date, votetitle as Title from votesession ")
				.append(" left join votesessionfaction on votesessionfaction.votesessionid = votesession.id ")
				.append(" left join individualvote on individualvote.votesessionfactionid = votesessionfaction.id ")
				.append(query.generateWhereClause(TITLE_COLUMNS, "votedate",
						"(votesessionfaction.votedyes*100)/(votesessionfaction.totalmembers-votesessionfaction.absent)>" + percent, "not voted = "
								+ VoteType.FOR.getId(), "not voted = " + VoteType.ABSENT.getId())).append(" order by voted, votedate, title").toString();
	}

	@Override
	protected DataBaseConnectionService getDBService() {
		return dbService;
	}
}
