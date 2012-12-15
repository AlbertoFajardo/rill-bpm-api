package nu.com.rill.analysis.report.excel.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nu.com.rill.analysis.report.excel.ReportEngine;

import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.zkoss.poi.ss.usermodel.Workbook;

@ContextConfiguration(value="classpath:/nu/com/rill/analysis/report/excel/export/applicationContext-re-mail.xml")
public class InlineMailContentExporterTest extends AbstractJUnit4SpringContextTests {

	@Autowired
	private SendReportViaEmailHelper sendReportViaEmailHelper;
	
	private EmbeddedDatabase db;
	
	@Before
    public void setUp() {
        // creates a H2 populated from default scripts classpath:schema.sql and classpath:test-data.sql
        db = new EmbeddedDatabaseBuilder().setType(EmbeddedDatabaseType.H2).addDefaultScripts().build();
        int count = new JdbcTemplate(db).queryForInt("SELECT count(*) as 今日访问量 from crm_tomcat_pangu where ts > DATEADD('DAY', -1, NOW()) and ts < NOW();");
        Assert.assertTrue(count > 0);
    }
	
	@After
    public void tearDown() {
        db.shutdown();
    }
	
	@Test
	public void export() {
		
		System.setProperty("re.jdbcData.driverClassName", "org.h2.Driver");
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/jdbcDataRetriever_table.xlsx");
		try {
			Map<String, String> contextParams = new HashMap<String, String>();
			contextParams.put(ReportEngine.URL, "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
			contextParams.put(ReportEngine.USERNAME, "sa");
			contextParams.put(ReportEngine.PASSWORD, "");
			
			Workbook wb = re.generateReport(cpr.getInputStream(), "jdbcDataRetrieverTest.xlsx", contextParams);
			HtmlExporter exporter = new HtmlExporter(wb, "jdbcDataRetriever_table.xlsx");
			
			// Start send via e-mail
			sendReportViaEmailHelper.export(exporter);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
