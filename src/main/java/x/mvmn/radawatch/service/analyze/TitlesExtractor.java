package x.mvmn.radawatch.service.analyze;

import java.util.List;

import x.mvmn.radawatch.service.db.DataBrowseQuery;

public interface TitlesExtractor {

	public List<String> getTitles(DataBrowseQuery query) throws Exception;

	public int getCount(DataBrowseQuery query) throws Exception;
}