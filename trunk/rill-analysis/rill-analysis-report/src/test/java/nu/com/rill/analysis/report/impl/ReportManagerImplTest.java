package nu.com.rill.analysis.report.impl;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.Map;

import nu.com.rill.analysis.report.ReportManager;
import nu.com.rill.analysis.report.bo.Report;
import nu.com.rill.analysis.report.excel.ReportEngine;
import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

import org.apache.commons.io.IOUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.annotation.Rollback;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;
import org.springframework.transaction.annotation.Transactional;

@ContextConfiguration(value="classpath:applicationContext-report-test.xml")
public class ReportManagerImplTest extends AbstractTransactionalJUnit4SpringContextTests {

	@Autowired
	private ReportManager reportMgr;
	
	@Test
	@Transactional
	@Rollback(value=false)
	public void insert() {
		
		ClassPathResource cpr = new ClassPathResource("test-luopan.xlsx");
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		try {
			IOUtils.copy(cpr.getInputStream(), baos);
		} catch (IOException e) {
			Assert.fail(e.getMessage());
		}
		
		System.setProperty(ReportEngine.URL, "http://localhost:8080/rill-analysis-report/web/");
		Report report = reportMgr.createReport("test-luopan.xlsx", null, baos.toByteArray());
		Map<String, Map<PARAM_CONFIG, String>> param = Report.deserializeParam(report.getParamsXStrem());
		Assert.assertNotNull(param);
		Assert.assertNotNull(param.get("商业产品线").get(PARAM_CONFIG.FETCH_URL).startsWith("http"));
	}

}
