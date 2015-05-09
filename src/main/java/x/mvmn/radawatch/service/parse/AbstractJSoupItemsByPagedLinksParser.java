package x.mvmn.radawatch.service.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import x.mvmn.radawatch.model.Entity;

public abstract class AbstractJSoupItemsByPagedLinksParser<T extends Entity> implements ItemsByPagedLinksParser<T> {

	protected final int MAX_RETRIES = 3;
	protected final int HTTP_TIMEOUT_MILLIS = 30000;

	protected Document doGet(final String url) throws Exception {
		return Jsoup.connect(url).timeout(HTTP_TIMEOUT_MILLIS).get();
	}

	public Document get(final String url) throws Exception {
		Document result = null;
		int tryNumber = 0;
		while (result == null && tryNumber++ < MAX_RETRIES) {
			try {
				result = doGet(url);
			} catch (final Exception e) {
				if (tryNumber < MAX_RETRIES) {
					System.err.println("Error fetching URL " + url + " (" + e.getClass().getName() + " " + e.getMessage() + ") - retrying (attempt #"
							+ (tryNumber + 1) + ").");
				} else {
					throw new Exception("Failed to get data with " + tryNumber + " retries from URL: " + url, e);
				}
			}
		}
		return result;
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
