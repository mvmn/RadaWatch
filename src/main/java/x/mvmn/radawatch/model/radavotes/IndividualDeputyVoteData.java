package x.mvmn.radawatch.model.radavotes;

import java.util.HashMap;
import java.util.Map;

import x.mvmn.radawatch.model.Entity;

public class IndividualDeputyVoteData extends Entity {

	public static enum VoteType {
		FOR(1), AGAINST(2), ABSTAINED(3), SKIPPED(4), ABSENT(5);
		private final int id;

		VoteType(final int id) {
			this.id = id;
		}

		public int getId() {
			return this.id;
		}

		public static VoteType valueById(final String id) {
			VoteType result = null;
			for (VoteType candidate : VoteType.values()) {
				if (id.equals(String.valueOf(candidate.id))) {
					result = candidate;
					break;
				}
			}
			return result;
		}
	}

	private static final Map<String, IndividualDeputyVoteData.VoteType> mapVoteNameToVoteType;
	static {
		mapVoteNameToVoteType = new HashMap<String, IndividualDeputyVoteData.VoteType>();
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
	private final IndividualDeputyVoteData.VoteType vote;

	public IndividualDeputyVoteData(final int dbId, final String name, final String vote) {
		super(dbId);
		this.name = name;
		IndividualDeputyVoteData.VoteType voteVal = mapVoteNameToVoteType.get(vote.toLowerCase().replaceAll("\\*", ""));
		if (voteVal == null) {
			voteVal = IndividualDeputyVoteData.VoteType.valueById(vote);
		}

		if (voteVal == null) {
			throw new RuntimeException("Unexpected vote value: " + vote);
		} else {
			this.vote = voteVal;
		}
	}

	public String getName() {
		return name;
	}

	public IndividualDeputyVoteData.VoteType getVote() {
		return vote;
	}

	@Override
	public String toString() {
		return "Vote [name=" + name + ", vote=" + vote + "]";
	}
}