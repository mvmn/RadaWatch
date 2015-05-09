package x.mvmn.radawatch.service.parse.presdecrees;

import java.sql.Date;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashSet;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Set;
import java.util.regex.Matcher;
import java.util.regex.Pattern;

import org.jsoup.nodes.Document;
import org.jsoup.nodes.Element;
import org.jsoup.nodes.Node;
import org.jsoup.nodes.TextNode;
import org.jsoup.select.Elements;
import org.jsoup.select.NodeVisitor;

import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.service.parse.AbstractJSoupItemsByPagedLinksParser;

public class PredisentialDecreesParser extends AbstractJSoupItemsByPagedLinksParser<PresidentialDecree> {

	private final static String PAGE_URL_STR_PATTERN = "http://www.president.gov.ua/documents/index.php?start=%s&cat=-1&search_string=&logic=all&like=begin&number=&from_day=1&from_month=1&from_year=1994&till_day=31&till_month=12&till_year=2100&order=desc";
	private final static String ITEM_URL_STR_PATTERN = "http://www.president.gov.ua/documents/%s.html?PrintVersion";
	private int pageSize = 10;
	private static final Pattern PAGE_ID_IN_URL_REGEX_PATTERN = Pattern.compile(".*/(\\d+)\\.html(?:\\?.*|$)");

	private final DateFormat dateFormat = new SimpleDateFormat("dd MMMM yyyy", new Locale("uk"));

	@Override
	public int parseOutTotalPagesCount() throws Exception {
		Document firstPage = get(String.format(PAGE_URL_STR_PATTERN, 0));
		pageSize = firstPage.select(".docs_list p:has(.head)").size();
		return Integer.parseInt(firstPage.select(".page_split_bar a").last().text().trim());
	}

	@Override
	public List<ItemLinkData<PresidentialDecree>> parseOutItemsLinksData(int pageNumber) throws Exception {
		final Document page = get(String.format(PAGE_URL_STR_PATTERN, (pageNumber - 1) * pageSize));
		final Elements itemsHeadlines = page.select(".docs_list p:has(.head)");
		final List<ItemLinkData<PresidentialDecree>> result = new ArrayList<ItemLinkData<PresidentialDecree>>(itemsHeadlines.size());
		for (Element itemHeadlineElem : itemsHeadlines) {
			final String type = cleanText(itemHeadlineElem.select(".head").first().ownText()).replaceAll("[ ]*№", "").trim();
			String numberCode = "";
			final Element numberCodeElem = itemHeadlineElem.select(".head b").first();
			if (numberCodeElem != null) {
				numberCode = cleanText(numberCodeElem.text()).trim();
			}
			final String dateStr = cleanText(itemHeadlineElem.select(".date").first().text()).trim();
			final String title = cleanText(itemHeadlineElem.select("a").first().text()).trim();
			final String href = cleanText(itemHeadlineElem.select("a").first().attr("href")).trim();

			final Matcher idMatcher = PAGE_ID_IN_URL_REGEX_PATTERN.matcher(href);
			idMatcher.find();
			final int id = Integer.parseInt(idMatcher.group(1));
			final ItemLinkData<PresidentialDecree> itemLinkData = new ItemLinkData<PresidentialDecree>(href, id);
			itemLinkData.getAdditionalData().put("decreeType", type.toLowerCase());
			itemLinkData.getAdditionalData().put("numberCode", numberCode);
			itemLinkData.getAdditionalData().put("dateStr", dateStr);
			itemLinkData.getAdditionalData().put("title", title);
			result.add(itemLinkData);
		}
		return result;
	}

	private static final Set<String> BLOCK_TAG_NAMES;
	static {
		final Set<String> blockTagNames = new HashSet<String>();
		blockTagNames.add("div");
		blockTagNames.add("p");
		blockTagNames.add("h1");
		blockTagNames.add("h2");
		blockTagNames.add("h3");
		blockTagNames.add("h4");
		blockTagNames.add("h5");
		blockTagNames.add("h6");
		blockTagNames.add("h7");
		blockTagNames.add("br");
		blockTagNames.add("td");

		BLOCK_TAG_NAMES = Collections.unmodifiableSet(blockTagNames);
	}

	@Override
	public PresidentialDecree parseOutItem(ItemLinkData<PresidentialDecree> itemLinkData) throws Exception {
		final Document page = get(String.format(ITEM_URL_STR_PATTERN, itemLinkData.getItemSiteId()));

		final StringBuilder itemFullText = new StringBuilder();
		page.select("#content").traverse(new NodeVisitor() {
			@Override
			public void tail(Node node, int depth) {
				if (node instanceof TextNode) {
					itemFullText.append(cleanTextPreserveLineBreaks(((TextNode) node).text()));
				}
			}

			@Override
			public void head(Node node, int depth) {
				if (node instanceof Element) {
					Element elem = (Element) node;
					if (BLOCK_TAG_NAMES.contains(elem.tagName().toLowerCase())) {
						itemFullText.append("\n");
					}
				}
			}
		});
		final Map<String, String> itemData = itemLinkData.getAdditionalData();
		final java.util.Date utilDate = dateFormat.parse(itemData.get("dateStr").toLowerCase());
		final Date date = new Date(utilDate.getTime());
		return new PresidentialDecree(-1, itemLinkData.getItemSiteId(), itemData.get("decreeType"), itemData.get("title"), date, itemData.get("numberCode"),
				itemFullText.toString().trim());
	}

	public static void main(String args[]) throws Exception {
		System.out.println((int) ' ');
		PredisentialDecreesParser testee = new PredisentialDecreesParser();
		System.out.println(testee.parseOutTotalPagesCount());
		System.out.println(testee.pageSize);
		for (ItemLinkData<PresidentialDecree> itemLinkData : testee.parseOutItemsLinksData(1)) {
			System.out.println(itemLinkData.toString());
			System.out.println(testee.parseOutItem(itemLinkData));
		}
	}

	// "http://www.president.gov.ua/documents/index.php?start=0&cat=-1&search_string=&logic=all&like=begin&number=&from_day=1&from_month=1&from_year=1994&till_day=31&till_month=12&till_year=2100&order=desc"

}
