package x.mvmn.radawatch.service.parse;

import java.util.ArrayList;
import java.util.List;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;

public class MeetingsListParser {

	private static final String PAGE_URL_PATTERN = "http://iportal.rada.gov.ua/news/hpz/page/%s";

	public static interface RecordExistenceChecker {
		public boolean checkExists(int meetingId);
	}

	private final int lastPageNumber;

	public MeetingsListParser() throws Exception {
		Document document = Jsoup.connect(String.format(PAGE_URL_PATTERN, 1)).timeout(30000).get();
		lastPageNumber = Integer.parseInt(document.select(".pages li:not(:contains(наступна))").last().text());
	}

	private static final Pattern pageIdPattern = Pattern.compile(".*(?:\\?|&)g_id=(\\d+)(?:&.*|$)");

	public List<VoteResultsPageDocument> fetchNewMeetings(final RecordExistenceChecker existenceChecker) throws Exception {
		List<VoteResultsPageDocument> meetings = new ArrayList<VoteResultsPageDocument>();
		for (int i = 1; i < lastPageNumber + 3; i++) {
			Document document = Jsoup.connect(String.format(PAGE_URL_PATTERN, i)).timeout(30000).get();
			for (Element link : document.select(".news_item .details a")) {
				Matcher idMatcher = pageIdPattern.matcher(link.attr("href"));
				idMatcher.find();
				int id = Integer.parseInt(idMatcher.group(1));
				if (!existenceChecker.checkExists(id)) {
					meetings.add(new VoteResultsPageDocument(link.attr("href").trim()));
				}
			}
		}
		return meetings;
	}

	public static void main(String[] args) throws Exception {
		System.out.println(new MeetingsListParser().lastPageNumber);
	}
}
