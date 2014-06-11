package x.mvmn.radawatch.service.analyze;

import java.sql.Date;
import java.util.List;

public interface TitlesAnalyzer {

	public List<String> getTitles(java.util.Date fromDate, java.util.Date toDate, String titleFilter) throws Exception;

	public int getCount(Date fromDate, Date toDate, String titleFilter) throws Exception;

	public int getCount(Integer offset, Integer count) throws Exception;

	public List<String> getTitles(Date fromDate, Date toDate, String titleFilter) throws Exception;

	public List<String> getTitles(Date fromDate, Date toDate, boolean appendDates, String titleFilter) throws Exception;

	public List<String> getTitles(Integer offset, Integer count, String titleFilter) throws Exception;

	public List<String> getTitles(Integer offset, Integer count, boolean appendDates, String titleFilter) throws Exception;

}