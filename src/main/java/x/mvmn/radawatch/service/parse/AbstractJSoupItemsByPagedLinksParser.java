package x.mvmn.radawatch.service.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import x.mvmn.radawatch.model.Entity;

public abstract class AbstractJSoupItemsByPagedLinksParser<T extends Entity> implements ItemsByPagedLinksParser<T> {

	protected final int MAX_RETRIES = 5;
	protected final int HTTP_TIMEOUT_MILLIS = 30000;
	protected final int RETRY_DELAY = 5000;
	protected final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/600.6.3 (KHTML, like Gecko) Version/8.0.6 Safari/600.6.3";

	protected Document doGet(final String url) throws Exception {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(HTTP_TIMEOUT_MILLIS).get();
	}

	public Document get(final String url) throws Exception {
		Document result = null;
		int tryNumber = 0;
		while (result == null && tryNumber++ < MAX_RETRIES) {
			try {
				result = doGet(url);
			} catch (final Exception e) {
				if (tryNumber < MAX_RETRIES) {
					int retryDelay = RETRY_DELAY * tryNumber;
					System.err.println("Error fetching URL " + url + " (" + e.getClass().getName() + " " + e.getMessage() + ") - will retry after "
							+ retryDelay / 1000 + " seconds (attempt #" + (tryNumber + 1) + ").");
					Thread.sleep(RETRY_DELAY);
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
