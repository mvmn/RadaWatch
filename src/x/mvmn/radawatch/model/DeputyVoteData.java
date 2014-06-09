package x.mvmn.radawatch.model;

import java.util.HashMap;
import java.util.Map;

public class DeputyVoteData extends DbEntry {

	public static enum VoteType {
		FOR(1), AGAINST(2), ABSTAINED(3), SKIPPED(4), ABSENT(5);
		private final int id;

		VoteType(final int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}
	}

	private static final Map<String, DeputyVoteData.VoteType> mapVoteNameToVoteType;
	static {
		mapVoteNameToVoteType = new HashMap<String, DeputyVoteData.VoteType>();
		mapVoteNameToVoteType.put("за", VoteType.FOR);
		mapVoteNameToVoteType.put("проти", VoteType.AGAINST);
		mapVoteNameToVoteType.put("утримався", VoteType.ABSTAINED);
		mapVoteNameToVoteType.put("утрималась", VoteType.ABSTAINED);
		mapVoteNameToVoteType.put("не голосував", VoteType.SKIPPED);
		mapVoteNameToVoteType.put("не голосувала", VoteType.SKIPPED);
		mapVoteNameToVoteType.put("відсутній", VoteType.ABSENT);
		mapVoteNameToVoteType.put("відсутня", VoteType.ABSENT);
	}

	private final String name;
	private final DeputyVoteData.VoteType vote;

	public DeputyVoteData(final int dbId, final String name, final String vote) {
		super(dbId);
		this.name = name;
		this.vote = mapVoteNameToVoteType.get(vote.toLowerCase().replaceAll("\\*", ""));
		if (this.vote == null) {
			throw new RuntimeException("Unexpected vote value: " + vote);
		}
	}

	public String getName() {
		return name;
	}

	public DeputyVoteData.VoteType getVote() {
		return vote;
	}

	@Override
	public String toString() {
		return "Vote [name=" + name + ", vote=" + vote + "]";
	}
}