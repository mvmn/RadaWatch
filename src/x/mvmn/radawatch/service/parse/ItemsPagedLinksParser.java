package x.mvmn.radawatch.service.parse;

import x.mvmn.radawatch.model.Entity;

public interface ItemsPagedLinksParser<T extends Entity> {

	public int parseTotalPagesCount() throws Exception;

	public int[] parseOutItemsSiteIds(int pageNumber) throws Exception;

	public T parseOutItem(int itemSiteId) throws Exception;
}
