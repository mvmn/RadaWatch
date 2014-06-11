package x.mvmn.radawatch.service.parse.presdecrees;

import x.mvmn.radawatch.model.presdecrees.PresidentialDecree;
import x.mvmn.radawatch.service.parse.ItemsByPagedLinksParser;

public class PredisentialDecreesParser implements ItemsByPagedLinksParser<PresidentialDecree> {

	// "http://www.president.gov.ua/documents/index.php?start=0&cat=-1&search_string=&logic=all&like=begin&number=&from_day=1&from_month=1&from_year=1994&till_day=31&till_month=12&till_year=2100&order=desc"

	@Override
	public int parseOutTotalPagesCount() throws Exception {
		// TODO Auto-generated method stub
		return 0;
	}

	@Override
	public int[] parseOutItemsSiteIds(int pageNumber) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

	@Override
	public PresidentialDecree parseOutItem(int itemSiteId) throws Exception {
		// TODO Auto-generated method stub
		return null;
	}

}
