package x.mvmn.radawatch.service.parse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class VoteResultsPageDocument {

	private final int globalId;
	private final String title;
	private final Boolean result;
	private final Date date;
	private final int votedYes;
	private final int votedNo;
	private final int abstained;
	private final int skipped;
	private final int total;
	private final List<Faction> factions = new ArrayList<Faction>();

	private final Pattern datePattern = Pattern.compile(".*[^\\d](\\d{1,2}.\\d{1,2}.\\d{4}\\s+\\d{1,2}:\\d{1,2})([^\\d].*|$)");
	private final Pattern totalsPattern = Pattern
			.compile(".*За:(\\d+)\\s+Проти:(\\d+)\\s+Утрималися:(\\d+)\\s+Не\\s+голосували:(\\d+)\\s+Всього:(\\d+)([^\\d].*|$)");
	private final Pattern factionTotalsPattern = Pattern
			.compile(".*За:(\\d+)\\s+Проти:(\\d+)\\s+Утрималися:(\\d+)\\s+Не\\s+голосували:(\\d+)\\s+Відсутні:(\\d+)([^\\d].*|$)");
	private final Pattern factionSizePattern = Pattern.compile(".*Кількість\\s+депутатів:\\s+([\\d]+)([^\\d].*|$)");
	private static final Pattern pageIdPattern = Pattern.compile(".*(?:\\?|&)g_id=(\\d+)(?:&.*|$)");

	private final DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy HH:mm");

	public VoteResultsPageDocument(String url) throws Exception {
		Matcher idMatcher = pageIdPattern.matcher(url);
		idMatcher.find();
		globalId = Integer.parseInt(idMatcher.group(1));

		Document document = Jsoup.connect(url).timeout(30000).get();
		title = document.select(".head_gol font[color=Black]").text().trim();
		String resultStr = document.select(".head_gol font[color=Red]").text().trim().toLowerCase()
				+ document.select(".head_gol font[color=Green]").text().trim().toLowerCase();
		if (resultStr.equals("рішення прийнято")) {
			result = true;
		} else if (resultStr.equals("рішення не прийнято")) {
			result = false;
		} else {
			throw new Exception("Unable to interpret result text: " + resultStr);
		}
		{
			String headingText = document.select(".head_gol").text();
			{
				Matcher dateMatcher = datePattern.matcher(headingText);
				if (dateMatcher.matches()) {
					date = dateFormat.parse(dateMatcher.group(1));
				} else {
					throw new Exception("Unable to find vote date in heading text: " + headingText);
				}
			}
			{
				Matcher totalsMatcher = totalsPattern.matcher(headingText);
				if (totalsMatcher.matches()) {
					votedYes = Integer.parseInt(totalsMatcher.group(1));
					votedNo = Integer.parseInt(totalsMatcher.group(2));
					abstained = Integer.parseInt(totalsMatcher.group(3));
					skipped = Integer.parseInt(totalsMatcher.group(4));
					total = Integer.parseInt(totalsMatcher.group(5));
				} else {
					throw new Exception("Unable to find vote date in heading text: " + headingText);
				}
			}
		}

		for (Element factionContainer : document.select("li[id=01] > ul > li")) {
			factions.add(praseFaction(factionContainer));
		}
	}

	public static class Vote {

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

		private static final Map<String, VoteType> mapVoteNameToVoteType;
		static {
			mapVoteNameToVoteType = new HashMap<String, VoteResultsPageDocument.Vote.VoteType>();
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
		private final VoteType vote;

		public Vote(final String name, final String vote) {
			this.name = name;
			this.vote = mapVoteNameToVoteType.get(vote.toLowerCase().replaceAll("\\*", ""));
			if (this.vote == null) {
				throw new RuntimeException("Unexpected vote value: " + vote);
			}
		}

		public String getName() {
			return name;
		}

		public VoteType getVote() {
			return vote;
		}

		@Override
		public String toString() {
			return "Vote [name=" + name + ", vote=" + vote + "]";
		}
	}

	public static class Faction {
		private final String title;
		private final int size;
		private final int votedYes;
		private final int votedNo;
		private final int abstained;
		private final int skipped;
		private final int absent;

		private final List<Vote> votes;

		public Faction(String title, int size, int votedYes, int votedNo, int abstained, int skipped, int absent, final List<Vote> votes) {
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

		public List<Vote> getVotes() {
			return votes;
		}
	}

	protected Faction praseFaction(Element factionContainer) {
		Faction result;
		String factionTitle = factionContainer.select(".frn b").text().trim();
		String factionHeading = factionContainer.select(".frn").text();
		Matcher factionSize = factionSizePattern.matcher(factionHeading);
		int factionSizeVal;
		if (factionSize.matches()) {
			factionSizeVal = Integer.parseInt(factionSize.group(1));
		} else {
			throw new RuntimeException("Failed to parse out faction size from heading text " + factionHeading);
		}
		Matcher factionTotals = factionTotalsPattern.matcher(factionHeading);
		List<Vote> factionVotes = new ArrayList<Vote>();
		for (Element voteElem : factionContainer.select(".frd li")) {
			factionVotes.add(new Vote(voteElem.select(".dep").text().trim(), voteElem.select(".golos").text().trim()));
		}
		if (factionVotes.size() < 1) {
			throw new RuntimeException("Zero votes matched for faction " + factionContainer);
		}

		if (factionTotals.matches()) {
			result = new Faction(factionTitle, factionSizeVal, Integer.parseInt(factionTotals.group(1)), Integer.parseInt(factionTotals.group(2)),
					Integer.parseInt(factionTotals.group(3)), Integer.parseInt(factionTotals.group(4)), Integer.parseInt(factionTotals.group(5)), factionVotes);
		} else {
			throw new RuntimeException("Failed to parse out faction totals from heading text " + factionHeading);
		}
		return result;
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

	public List<Faction> getFactions() {
		return factions;
	}

	public Pattern getDatePattern() {
		return datePattern;
	}

	public Pattern getTotalsPattern() {
		return totalsPattern;
	}

	public Pattern getFactionTotalsPattern() {
		return factionTotalsPattern;
	}

	public Pattern getFactionSizePattern() {
		return factionSizePattern;
	}

	public DateFormat getDateFormat() {
		return dateFormat;
	}

	public int getGlobalId() {
		return globalId;
	}

	// public static void main(String args[]) throws Exception {
	// VoteResultsPageDocument tst = new VoteResultsPageDocument("http://w1.c1.rada.gov.ua/pls/radan_gs09/ns_golos?g_id=5354");
	// System.out.println(tst.globalId);
	// System.out.println(tst.title);
	// System.out.println(tst.result);
	// System.out.println(tst.date);
	// System.out.println(tst.votedYes);
	// System.out.println(tst.votedNo);
	// System.out.println(tst.abstained);
	// System.out.println(tst.skipped);
	// System.out.println(tst.total);
	// for (Faction faction : tst.factions) {
	// System.out.println(faction.toString());
	// }
	// }
}
