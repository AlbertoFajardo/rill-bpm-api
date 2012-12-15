package nu.com.rill.analysis.report.excel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;
import java.util.Map.Entry;

import nu.com.rill.analysis.report.excel.export.HtmlExporter;

import org.apache.commons.io.FileUtils;
import org.junit.After;
import org.junit.Assert;
import org.junit.Before;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabase;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseBuilder;
import org.springframework.jdbc.datasource.embedded.EmbeddedDatabaseType;
import org.zkoss.poi.ss.usermodel.Workbook;

public class JdbcDataRetrieverTest {

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
	public void test() {
		
		System.setProperty("re.jdbcData.driverClassName", "org.h2.Driver");
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/jdbcDataRetriever_table.xlsx");
		try {
			Map<String, String> contextParams = new HashMap<String, String>();
			contextParams.put(ReportEngine.URL, "jdbc:h2:mem:testdb;DB_CLOSE_DELAY=-1");
			contextParams.put(ReportEngine.USERNAME, "sa");
			contextParams.put(ReportEngine.PASSWORD, "");
			
			Workbook wb = re.generateReport(cpr.getInputStream(), "jdbcDataRetrieverTest.xlsx", contextParams);
			ByteArrayOutputStream baosXlsx = new ByteArrayOutputStream();
			wb.write(baosXlsx);
			File tmpXlsx = File.createTempFile("jdbcDataRetrieverTest.xlsx" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpXlsx, baosXlsx.toByteArray());
			
			HtmlExporter exporter = new HtmlExporter(wb, "jdbcDataRetriever_table.xlsx");
			String html = exporter.export();
			int startIndex = html.indexOf("<title>");
			int endIndex = html.indexOf("</title>");
			if (startIndex > 0) {
				String title = html.substring(startIndex + 7, endIndex);
				System.out.println(title);
			}
			// Generate image file
			for (Entry<String, byte[]> entry : exporter.getImages().entrySet()) {
				File tmpImage = new File(System.getProperty("java.io.tmpdir"), entry.getKey());
				FileUtils.writeByteArrayToFile(tmpImage, entry.getValue());
			}
			
			File tmpHtml = File.createTempFile("jdbcDataRetrieverTest.xlsx" + System.currentTimeMillis(), ".html");
			FileUtils.writeByteArrayToFile(tmpHtml, html.getBytes("utf-8"));
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
