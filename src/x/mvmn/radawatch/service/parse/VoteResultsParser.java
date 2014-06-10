package x.mvmn.radawatch.service.parse;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import x.mvmn.radawatch.model.radavotes.DeputyVoteData;
import x.mvmn.radawatch.model.radavotes.VoteFactionData;
import x.mvmn.radawatch.model.radavotes.VoteResultsData;

public class VoteResultsParser {

	private final VoteResultsData data;

	private final Pattern datePattern = Pattern.compile(".*[^\\d](\\d{1,2}.\\d{1,2}.\\d{4}\\s+\\d{1,2}:\\d{1,2})([^\\d].*|$)");
	private final Pattern totalsPattern = Pattern
			.compile(".*За:(\\d+)\\s+Проти:(\\d+)\\s+Утрималися:(\\d+)\\s+Не\\s+голосували:(\\d+)\\s+Всього:(\\d+)([^\\d].*|$)");
	private final Pattern factionTotalsPattern = Pattern
			.compile(".*За:(\\d+)\\s+Проти:(\\d+)\\s+Утрималися:(\\d+)\\s+Не\\s+голосували:(\\d+)\\s+Відсутні:(\\d+)([^\\d].*|$)");
	private final Pattern factionSizePattern = Pattern.compile(".*Кількість\\s+депутатів:\\s+([\\d]+)([^\\d].*|$)");
	private static final Pattern pageIdPattern = Pattern.compile(".*(?:\\?|&)g_id=(\\d+)(?:&.*|$)");

	private final DateFormat dateFormat = new SimpleDateFormat("d.M.yyyy HH:mm");

	public VoteResultsParser(String url) throws Exception {
		Matcher idMatcher = pageIdPattern.matcher(url);
		idMatcher.find();
		List<VoteFactionData> factions = new ArrayList<VoteFactionData>();
		final int globalId = Integer.parseInt(idMatcher.group(1));

		Document document = Jsoup.connect(url).timeout(30000).get();
		final String title = document.select(".head_gol font[color=Black]").text().trim();
		String resultStr = document.select(".head_gol font[color=Red]").text().trim().toLowerCase()
				+ document.select(".head_gol font[color=Green]").text().trim().toLowerCase();
		final boolean result;
		if (resultStr.equals("рішення прийнято")) {
			result = true;
		} else if (resultStr.equals("рішення не прийнято")) {
			result = false;
		} else {
			throw new Exception("Unable to interpret result text: " + resultStr);
		}
		final Date date;
		final int votedYes;
		final int votedNo;
		final int abstained;
		final int skipped;
		final int total;

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

		data = new VoteResultsData(-1, globalId, title, result, date, votedYes, votedNo, abstained, skipped, total, factions);
	}

	protected VoteFactionData praseFaction(Element factionContainer) {
		VoteFactionData result;
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
		List<DeputyVoteData> factionVotes = new ArrayList<DeputyVoteData>();
		for (Element voteElem : factionContainer.select(".frd li")) {
			factionVotes.add(new DeputyVoteData(-1, voteElem.select(".dep").text().trim(), voteElem.select(".golos").text().trim()));
		}
		if (factionVotes.size() < 1) {
			throw new RuntimeException("Zero votes matched for faction " + factionContainer);
		}

		if (factionTotals.matches()) {
			result = new VoteFactionData(-1, factionTitle, factionSizeVal, Integer.parseInt(factionTotals.group(1)), Integer.parseInt(factionTotals.group(2)),
					Integer.parseInt(factionTotals.group(3)), Integer.parseInt(factionTotals.group(4)), Integer.parseInt(factionTotals.group(5)), factionVotes);
		} else {
			throw new RuntimeException("Failed to parse out faction totals from heading text " + factionHeading);
		}
		return result;
	}

	public VoteResultsData getVoteResultsData() {
		return this.data;
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
