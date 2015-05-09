package x.mvmn.radawatch.service.parse;

import org.jsoup.Jsoup;
import org.jsoup.nodes.Document;

import x.mvmn.radawatch.model.Entity;

public abstract class AbstractJSoupItemsByPagedLinksParser<T extends Entity> implements ItemsByPagedLinksParser<T> {

	protected final int MAX_RETRIES = 5;
	protected final int HTTP_TIMEOUT_MILLIS = 30000;
	protected final int RETRY_DELAY_SECONDS = 5;
	protected final boolean PROGRESSIVE_RETRY_DELAY = true;
	protected final String USER_AGENT = "Mozilla/5.0 (Macintosh; Intel Mac OS X 10_10_3) AppleWebKit/600.6.3 (KHTML, like Gecko) Version/8.0.6 Safari/600.6.3";

	protected Document doGet(final String url, int timeout) throws Exception {
		return Jsoup.connect(url).userAgent(USER_AGENT).timeout(timeout).get();
	}

	public Document get(final String url) throws Exception {
		return get(url, HTTP_TIMEOUT_MILLIS, MAX_RETRIES, RETRY_DELAY_SECONDS, PROGRESSIVE_RETRY_DELAY);
	}

	public Document get(final String url, final int httpTimoutMillis, final int maxRetries, final int retryDelaySeconds, final boolean progressiveRetryDelay)
			throws Exception {
		Document result = null;
		int tryNumber = 0;
		while (result == null && tryNumber++ < maxRetries + 1) {
			try {
				result = doGet(url, httpTimoutMillis);
			} catch (final Exception e) {
				if (tryNumber < maxRetries + 1) {
					int actualRetryDelaySeconds = retryDelaySeconds * (progressiveRetryDelay ? (int) Math.pow(2, tryNumber - 1) : 1);
					System.err.println(String.format("Error fetching URL %s (%s %s) - will retry after %s seconds (attempt #%s).", url, e.getClass().getName(),
							e.getMessage(), actualRetryDelaySeconds, (tryNumber + 1)));
					try {
						Thread.sleep(actualRetryDelaySeconds * 1000);
					} catch (final InterruptedException intterrupt) {
						System.err.println("Warning: Fetch retry delay interrupted.");
					}
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
