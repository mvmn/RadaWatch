package x.mvmn.radawatch.model.radavotes;

import java.util.List;

import x.mvmn.radawatch.model.SubEntityCapableEntity;

public class VoteSessionPerFactionData extends SubEntityCapableEntity<IndividualDeputyVoteData> {
	private final String title;
	private final int size;
	private final int votedYes;
	private final int votedNo;
	private final int abstained;
	private final int skipped;
	private final int absent;

	private final List<IndividualDeputyVoteData> votes;

	public VoteSessionPerFactionData(final int dbId, final String title, final int size, final int votedYes, final int votedNo, final int abstained,
			final int skipped, final int absent, final List<IndividualDeputyVoteData> votes) {
		super(dbId);
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

	public List<IndividualDeputyVoteData> getVotes() {
		return votes;
	}

	@Override
	public List<IndividualDeputyVoteData> getSubEntities() {
		return getVotes();
	}
}