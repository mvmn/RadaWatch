package x.mvmn.radawatch.model.radavotes;

import java.util.Date;
import java.util.List;

import x.mvmn.radawatch.model.SubEntityCapableEntity;

public class VoteSessionResultsData extends SubEntityCapableEntity<VoteSessionPerFactionData> {
	private final int globalId;
	private final String title;
	private final boolean result;
	private final Date date;
	private final int votedYes;
	private final int votedNo;
	private final int abstained;
	private final int skipped;
	private final int total;
	private final List<VoteSessionPerFactionData> factions;

	public VoteSessionResultsData(final int dbId, final int globalId, final String title, final boolean result, final Date date, final int votedYes,
			final int votedNo, final int abstained, final int skipped, final int total, final List<VoteSessionPerFactionData> factions) {
		super(dbId);
		this.globalId = globalId;
		this.title = title;
		this.result = result;
		this.date = date;
		this.votedYes = votedYes;
		this.votedNo = votedNo;
		this.abstained = abstained;
		this.skipped = skipped;
		this.total = total;
		this.factions = factions;
	}

	public int getGlobalId() {
		return globalId;
	}

	public String getTitle() {
		return title;
	}

	public Boolean getResult() {
		return result;
	}

	public Date getDate() {
		return date;
	}

	public int getVotedYes() {
		return votedYes;
	}

	public int getVotedNo() {
		return votedNo;
	}

	public int getAbstained() {
		return abstained;
	}

	public int getSkipped() {
		return skipped;
	}

	public int getTotal() {
		return total;
	}

	public List<VoteSessionPerFactionData> getFactions() {
		return factions;
	}

	@Override
	public List<VoteSessionPerFactionData> getSubEntities() {
		return getFactions();
	}

}