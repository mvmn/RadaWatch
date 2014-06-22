package x.mvmn.radawatch.gui;

public interface PagedFetchProgressIndicator {

	public void reset();

	public void setPagesCount(int pagesCount);

	public void setItemsCount(int itemsCount);

	public void setPagesProgress(int currentPage);

	public void setItemsProgress(int currentItem);

}
