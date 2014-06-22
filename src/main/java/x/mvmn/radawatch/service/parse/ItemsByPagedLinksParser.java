package x.mvmn.radawatch.service.parse;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import x.mvmn.radawatch.model.Entity;

public interface ItemsByPagedLinksParser<T extends Entity> {

	public static class ItemLinkData<T> {
		protected final String url;
		protected final int itemSiteId;
		protected final Map<String, String> additionalData = new HashMap<String, String>();

		public ItemLinkData(String url, int itemSiteId) {
			super();
			this.url = url;
			this.itemSiteId = itemSiteId;
		}

		public String getUrl() {
			return url;
		}

		public int getItemSiteId() {
			return itemSiteId;
		}

		public Map<String, String> getAdditionalData() {
			return additionalData;
		}

		public String toString() {
			return String.format("ItemLinkData: id = %s, url = %s, additional data = %s", itemSiteId, url, additionalData);
		}
	}

	public int parseOutTotalPagesCount() throws Exception;

	public List<ItemLinkData<T>> parseOutItemsLinksData(int pageNumber) throws Exception;

	public T parseOutItem(ItemLinkData<T> itemLinkData) throws Exception;
}
