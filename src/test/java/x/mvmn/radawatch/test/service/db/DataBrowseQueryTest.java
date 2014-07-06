package x.mvmn.radawatch.test.service.db;

import java.sql.Date;

import org.junit.Assert;
import org.junit.Test;

import x.mvmn.radawatch.service.db.DataBrowseQuery;

public class DataBrowseQueryTest {

	@Test
	public void testEmptyClauses() {
		DataBrowseQuery test = new DataBrowseQuery(null, null, null, null, null);
		Assert.assertEquals("", normalizeSpaces(test.generateLimitClause()));
		Assert.assertEquals("", normalizeSpaces(test.generateWhereClause("title", "date", true)));
		Assert.assertEquals("", normalizeSpaces(test.generateWhereClause("title", "date", false)));
	}

	@Test
	public void testLimitClauseGeneration() {
		Assert.assertEquals("LIMIT 33,-1", normalizeSpaces(new DataBrowseQuery(null, 33, null, null, null).generateLimitClause()));
		Assert.assertEquals("LIMIT 33", normalizeSpaces(new DataBrowseQuery(null, null, 33, null, null).generateLimitClause()));
		Assert.assertEquals("LIMIT 12,34", normalizeSpaces(new DataBrowseQuery(null, 12, 34, null, null).generateLimitClause()));
	}

	@Test
	public void testWhereDatesClauseGeneration() {
		final Date date = new Date(1403409053282L);
		Assert.assertEquals("WHERE date>='" + date.toString() + "'",
				normalizeSpaces(new DataBrowseQuery(null, null, null, date, null).generateWhereClause("title", "date", true)));
		Assert.assertEquals("date>='" + date.toString() + "'",
				normalizeSpaces(new DataBrowseQuery(null, null, null, date, null).generateWhereClause("title", "date", false)));
		Assert.assertEquals("WHERE date<'" + date.toString() + "'",
				normalizeSpaces(new DataBrowseQuery(null, null, null, null, date).generateWhereClause("title", "date", true)));
		Assert.assertEquals("date<'" + date.toString() + "'",
				normalizeSpaces(new DataBrowseQuery(null, null, null, null, date).generateWhereClause("title", "date", false)));
		Assert.assertEquals("WHERE date>='" + date.toString() + "' AND date<'" + date.toString() + "'", normalizeSpaces(new DataBrowseQuery(null, null, null,
				date, date).generateWhereClause("title", "date", true)));
		Assert.assertEquals("date>='" + date.toString() + "' AND date<'" + date.toString() + "'", normalizeSpaces(new DataBrowseQuery(null, null, null, date,
				date).generateWhereClause("title", "date", false)));
	}

	@Test
	public void testSearchPhraseClauseGeneration() {
		Assert.assertEquals("", normalizeSpaces(new DataBrowseQuery("", null, null, null, null).generateWhereClause("title", "date", true)));
		Assert.assertEquals("", normalizeSpaces(new DataBrowseQuery("%", null, null, null, null).generateWhereClause("title", "date", true)));
		Assert.assertEquals("WHERE ( (title like '%uh % oh%') OR (0=1))",
				normalizeSpaces(new DataBrowseQuery("uh % oh", null, null, null, null).generateWhereClause("title", "date", true)));
		Assert.assertEquals("( (title like '%uh % oh%') OR (0=1))",
				normalizeSpaces(new DataBrowseQuery("uh % oh", null, null, null, null).generateWhereClause("title", "date", false)));
		Assert.assertEquals("WHERE ( (title like '%uh % oh%') OR (title like '%eh % he%') OR (0=1))", normalizeSpaces(new DataBrowseQuery("uh % oh|eh % he",
				null, null, null, null).generateWhereClause("title", "date", true)));
		Assert.assertEquals("( (title like '%uh % oh%') OR (title like '%eh % he%') OR (0=1))", normalizeSpaces(new DataBrowseQuery("uh % oh|eh % he", null,
				null, null, null).generateWhereClause("title", "date", false)));
	}

	@Test
	public void testWhereFullClauseGeneration() {
		final Date date = new Date(1403409053282L);
		Assert.assertEquals("WHERE ( (title like '%uh % oh%') OR (0=1)) AND date>='" + date.toString() + "'", normalizeSpaces(new DataBrowseQuery("uh % oh",
				null, null, date, null).generateWhereClause("title", "date", true)));
		Assert.assertEquals("( (title like '%uh % oh%') OR (0=1)) AND date>='" + date.toString() + "'", normalizeSpaces(new DataBrowseQuery("uh % oh", null,
				null, date, null).generateWhereClause("title", "date", false)));
		Assert.assertEquals("WHERE ( (title like '%uh % oh%') OR (0=1)) AND date<'" + date.toString() + "'", normalizeSpaces(new DataBrowseQuery("uh % oh",
				null, null, null, date).generateWhereClause("title", "date", true)));
		Assert.assertEquals("( (title like '%uh % oh%') OR (0=1)) AND date<'" + date.toString() + "'", normalizeSpaces(new DataBrowseQuery("uh % oh", null,
				null, null, date).generateWhereClause("title", "date", false)));
		Assert.assertEquals("WHERE ( (title like '%uh % oh%') OR (0=1)) AND date>='" + date.toString() + "' AND date<'" + date.toString() + "'",
				normalizeSpaces(new DataBrowseQuery("uh % oh", null, null, date, date).generateWhereClause("title", "date", true)));
		Assert.assertEquals("( (title like '%uh % oh%') OR (0=1)) AND date>='" + date.toString() + "' AND date<'" + date.toString() + "'",
				normalizeSpaces(new DataBrowseQuery("uh % oh", null, null, date, date).generateWhereClause("title", "date", false)));

		Assert.assertEquals(
				"WHERE ( (title like '%uh % oh%') OR (title like '%eh % he%') OR (0=1)) AND date>='" + date.toString() + "' AND date<'" + date.toString() + "'",
				normalizeSpaces(new DataBrowseQuery("uh % oh|eh % he", null, null, date, date).generateWhereClause("title", "date", true)));
		Assert.assertEquals("( (title like '%uh % oh%') OR (title like '%eh % he%') OR (0=1)) AND date>='" + date.toString() + "' AND date<'" + date.toString()
				+ "'", normalizeSpaces(new DataBrowseQuery("uh % oh|eh % he", null, null, date, date).generateWhereClause("title", "date", false)));
	}

	private String normalizeSpaces(String txt) {
		if (txt == null) {
			return null;
		} else {
			return txt.trim().replaceAll("[ ]+", " ");
		}
	}
}
