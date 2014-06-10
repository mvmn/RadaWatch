package x.mvmn.radawatch.service.parse;

import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

import x.mvmn.radawatch.service.db.radavotes.RadaVotesStorageService;

public class MeetingsListParser {

	private static final String PAGE_URL_PATTERN = "http://iportal.rada.gov.ua/news/hpz/page/%s";

	private final int lastPageNumber;

	public MeetingsListParser() throws Exception {
		Document document = Jsoup.connect(String.format(PAGE_URL_PATTERN, 1)).timeout(30000).get();
		lastPageNumber = Integer.parseInt(document.select(".pages li:not(:contains(наступна))").last().text());
	}

	private static final Pattern pageIdPattern = Pattern.compile(".*(?:\\?|&)g_id=(\\d+)(?:&.*|$)");

	public int fetchNewMeetings(final int pageNumber, RadaVotesStorageService vrStore) throws Exception {
		int result = 0;
		Document document = Jsoup.connect(String.format(PAGE_URL_PATTERN, pageNumber)).timeout(30000).get();
		for (Element link : document.select(".archieve_block .news_item .details a")) {
			Matcher idMatcher = pageIdPattern.matcher(link.attr("href"));
			idMatcher.find();
			int id = Integer.parseInt(idMatcher.group(1));
			if (!vrStore.checkExists(id)) {
				vrStore.storeNewRecord(new VoteResultsParser(link.attr("href").trim()).getVoteResultsData());
				result++;
				System.out.println("Fetched meeting data for ID " + id);
			}
		}

		return result;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(new MeetingsListParser().lastPageNumber);
	}

	public int getLastPageNumber() {
		return lastPageNumber;
	}
}
