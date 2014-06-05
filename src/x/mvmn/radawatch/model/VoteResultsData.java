package x.mvmn.radawatch.model;

import java.util.Date;
import java.util.List;

public class VoteResultsData {
	private final int globalId;
	private final String title;
	private final Boolean result;
	private final Date date;
	private final int votedYes;
	private final int votedNo;
	private final int abstained;
	private final int skipped;
	private final int total;
	private final List<VoteFactionData> factions;

	public VoteResultsData(int globalId, String title, Boolean result, Date date, int votedYes, int votedNo, int abstained, int skipped, int total,
			List<VoteFactionData> factions) {
		super();
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

	public List<VoteFactionData> getFactions() {
		return factions;
	}

}