package x.mvmn.radawatch.service.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import x.mvmn.radawatch.model.Entity;

public abstract class AbstractJSoupItemsByPagedLinksParser<T extends Entity> implements ItemsByPagedLinksParser<T> {

	public Document get(final String url) throws Exception {
		try {
			return Jsoup.connect(url).timeout(30000).get();
		} catch (final Exception e) {
			throw new Exception("Failed to get data from URL " + url, e);
		}
	}

	public String cleanTextPreserveLineBreaks(String str) {
		if (str != null) {
			return str.replaceAll("[\u00A0 ]+", " ");
		} else {
			return null;
		}
	}

	public String cleanText(String str) {
		if (str != null) {
			return str.replaceAll("[\u00A0\\s]+", " ");
		} else {
			return null;
		}
	}
}
