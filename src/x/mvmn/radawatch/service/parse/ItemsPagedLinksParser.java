package x.mvmn.radawatch.service.parse;

import java.util.List;

import x.mvmn.radawatch.model.Entity;

public interface ItemsPagedLinksParser<T extends Entity> {

	public int parseTotalPagesCount();

	public List<String> parseOutItemsLinks(int pageNumber);

	public T parseOutItem(String itemLinks);
}
