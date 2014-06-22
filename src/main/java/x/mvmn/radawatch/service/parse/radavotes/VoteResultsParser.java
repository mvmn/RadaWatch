package x.mvmn.radawatch.service.parse.radavotes;

import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Date;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import x.mvmn.radawatch.model.radavotes.IndividualDeputyVoteData;
import x.mvmn.radawatch.model.radavotes.VoteSessionPerFactionData;
import x.mvmn.radawatch.model.radavotes.VoteSessionResultsData;
import x.mvmn.radawatch.service.parse.AbstractJSoupItemsByPagedLinksParser;

public class VoteResultsParser extends AbstractJSoupItemsByPagedLinksParser<VoteSessionResultsData> {

	private static final String PAGE_URL_STR_PATTERN = "http://iportal.rada.gov.ua/news/hpz/page/%s";
	private static final String ITEM_URL_STR_PATTERN = "http://w1.c1.rada.gov.ua/pls/radan_gs09/ns_golos?g_id=%s";

	private static final Pattern DATE_REGEX_PATTERN = Pattern.compile(".*[^\\d](\\d{1,2}.\\d{1,2}.\\d{4}\\s+\\d{1,2}:\\d{1,2})([^\\d].*|$)");
	private static final Pattern TOTALS_REGEX_PATTERN = Pattern
			.compile(".*За:(\\d+)\\s+Проти:(\\d+)\\s+Утрималися:(\\d+)\\s+Не\\s+голосували:(\\d+)\\s+Всього:(\\d+)([^\\d].*|$)");
	private static final Pattern FACTION_TOTALS_REGEX_PATTERN = Pattern
			.compile(".*За:(\\d+)\\s+Проти:(\\d+)\\s+Утрималися:(\\d+)\\s+Не\\s+голосували:(\\d+)\\s+Відсутні:(\\d+)([^\\d].*|$)");
	private static final Pattern FACTION_SIZE_REGEX_PATTERN = Pattern.compile(".*Кількість\\s+депутатів:\\s+([\\d]+)([^\\d].*|$)");
	private static final Pattern PAGE_ID_IN_URL_REGEX_PATTERN = Pattern.compile(".*(?:\\?|&)g_id=(\\d+)(?:&.*|$)");

	private final DateFormat siteDateFormat = new SimpleDateFormat("d.M.yyyy HH:mm");

	@Override
	public int parseOutTotalPagesCount() throws Exception {
		Document document = get(String.format(PAGE_URL_STR_PATTERN, 1));
		return Integer.parseInt(document.select(".pages li:not(:contains(наступна))").last().text());
	}

	@Override
	public List<ItemLinkData<VoteSessionResultsData>> parseOutItemsLinksData(final int pageNumber) throws Exception {
		List<ItemLinkData<VoteSessionResultsData>> result = new ArrayList<ItemLinkData<VoteSessionResultsData>>();
		Document document = get(String.format(PAGE_URL_STR_PATTERN, pageNumber));
		for (Element link : document.select(".archieve_block .news_item .details a")) {
			String href = link.attr("href");
			Matcher idMatcher = PAGE_ID_IN_URL_REGEX_PATTERN.matcher(href);
			idMatcher.find();
			int id = Integer.parseInt(idMatcher.group(1));
			result.add(new ItemLinkData<VoteSessionResultsData>(href, id));
		}
		return result;
	}

	@Override
	public VoteSessionResultsData parseOutItem(final ItemLinkData<VoteSessionResultsData> itemLinkData) throws Exception {
		return parseVoteResults(itemLinkData.getItemSiteId());
	}

	public VoteSessionResultsData parseVoteResults(final int id) throws Exception {
		final VoteSessionResultsData data;

		List<VoteSessionPerFactionData> factions = new ArrayList<VoteSessionPerFactionData>();
		final int globalId = id;

		Document document = get(String.format(ITEM_URL_STR_PATTERN, id));
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
				Matcher dateMatcher = DATE_REGEX_PATTERN.matcher(headingText);
				if (dateMatcher.matches()) {
					date = siteDateFormat.parse(dateMatcher.group(1));
				} else {
					throw new Exception("Unable to find vote date in heading text: " + headingText);
				}
			}
			{
				Matcher totalsMatcher = TOTALS_REGEX_PATTERN.matcher(headingText);
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

		data = new VoteSessionResultsData(-1, globalId, title, result, date, votedYes, votedNo, abstained, skipped, total, factions);
		return data;
	}

	protected VoteSessionPerFactionData praseFaction(Element factionContainer) {
		VoteSessionPerFactionData result;
		String factionTitle = factionContainer.select(".frn b").text().trim();
		String factionHeading = factionContainer.select(".frn").text();
		Matcher factionSize = FACTION_SIZE_REGEX_PATTERN.matcher(factionHeading);
		int factionSizeVal;
		if (factionSize.matches()) {
			factionSizeVal = Integer.parseInt(factionSize.group(1));
		} else {
			throw new RuntimeException("Failed to parse out faction size from heading text " + factionHeading);
		}
		Matcher factionTotals = FACTION_TOTALS_REGEX_PATTERN.matcher(factionHeading);
		List<IndividualDeputyVoteData> factionVotes = new ArrayList<IndividualDeputyVoteData>();
		for (Element voteElem : factionContainer.select(".frd li")) {
			factionVotes.add(new IndividualDeputyVoteData(-1, voteElem.select(".dep").text().trim(), voteElem.select(".golos").text().trim()));
		}
		if (factionVotes.size() < 1) {
			throw new RuntimeException("Zero votes matched for faction " + factionContainer);
		}

		if (factionTotals.matches()) {
			result = new VoteSessionPerFactionData(-1, factionTitle, factionSizeVal, Integer.parseInt(factionTotals.group(1)), Integer.parseInt(factionTotals
					.group(2)), Integer.parseInt(factionTotals.group(3)), Integer.parseInt(factionTotals.group(4)), Integer.parseInt(factionTotals.group(5)),
					factionVotes);
		} else {
			throw new RuntimeException("Failed to parse out faction totals from heading text " + factionHeading);
		}
		return result;
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
