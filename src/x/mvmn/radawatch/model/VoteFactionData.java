package x.mvmn.radawatch.model;

import java.util.List;

public class VoteFactionData {
	private final String title;
	private final int size;
	private final int votedYes;
	private final int votedNo;
	private final int abstained;
	private final int skipped;
	private final int absent;

	private final List<DeputyVoteData> votes;

	public VoteFactionData(String title, int size, int votedYes, int votedNo, int abstained, int skipped, int absent, final List<DeputyVoteData> votes) {
		this.title = title;
		this.size = size;
		this.votedYes = votedYes;
		this.votedNo = votedNo;
		this.abstained = abstained;
		this.skipped = skipped;
		this.absent = absent;
		this.votes = votes;
	}

	public String getTitle() {
		return title;
	}

	public int getSize() {
		return size;
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

	public int getAbsent() {
		return absent;
	}

	@Override
	public String toString() {
		return "Faction [title=" + title + ", size=" + size + ", votedYes=" + votedYes + ", votedNo=" + votedNo + ", abstained=" + abstained + ", skipped="
				+ skipped + ", absent=" + absent + ", votes=" + votes + "]";
	}

	public List<DeputyVoteData> getVotes() {
		return votes;
	}
}