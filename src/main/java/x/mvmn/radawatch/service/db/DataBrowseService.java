package x.mvmn.radawatch.service.db;

import java.util.List;

import x.mvmn.radawatch.model.Entity;

public interface DataBrowseService<T extends Entity> {

	public T fetchItem(int itemDbId) throws Exception;

	public int countItems(int parentItemDbId, DataBrowseQuery query) throws Exception;

	public List<T> fetchItems(int parentItemDbId, DataBrowseQuery query) throws Exception;

	public List<T> fetchItems(int parentItemDbId, DataBrowseQuery query, boolean fetchFullData) throws Exception;

}
