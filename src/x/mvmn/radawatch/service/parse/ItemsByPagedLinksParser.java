package x.mvmn.radawatch.service.parse;

import x.mvmn.radawatch.model.Entity;

public interface ItemsByPagedLinksParser<T extends Entity> {

	public int parseOutTotalPagesCount() throws Exception;

	public int[] parseOutItemsSiteIds(int pageNumber) throws Exception;

	public T parseOutItem(int itemSiteId) throws Exception;
}
