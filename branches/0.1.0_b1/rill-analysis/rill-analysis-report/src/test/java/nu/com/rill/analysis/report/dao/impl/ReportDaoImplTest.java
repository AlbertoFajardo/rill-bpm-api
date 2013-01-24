package nu.com.rill.analysis.report.dao.impl;

import nu.com.rill.analysis.report.bo.Report;
import nu.com.rill.analysis.report.dao.ReportDao;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(value="classpath:applicationContext-report-test.xml")
public class ReportDaoImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private ReportDao reportDao;
	
	@Test
	@Transactional
	@Rollback(value=false)
	public void insert() {
		
		reportDao.insertReport("test", "param", "0 0 0 * 1", "bytes".getBytes());
		reportDao.insertReport("testNoCronExpression", "param", null, "bytes".getBytes());
	}
	
	@Test
	@Transactional
	public void query() {
		
		Report report = reportDao.getReportByName("test");
		Assert.assertNotNull(report);
		Assert.assertNotNull(report.getParamsXStrem());
		Assert.assertNotNull(report.getReportContent());
	}

}
