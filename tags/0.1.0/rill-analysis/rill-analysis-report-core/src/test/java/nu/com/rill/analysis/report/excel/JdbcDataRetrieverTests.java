package nu.com.rill.analysis.report.excel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

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

public class JdbcDataRetrieverTests {

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
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/jdbcDataRetriever.xlsx");
		try {
			Map<String, String> contextParams = new HashMap<String, String>();
			contextParams.put(ReportEngine.URL, "jdbc:h2:mem:testDb;DB_CLOSE_DELAY=1000;MVCC=TRUE");
			contextParams.put(ReportEngine.USERNAME, "sa");
			contextParams.put(ReportEngine.PASSWORD, "");
			
			Workbook wb = re.generateReport(cpr.getInputStream(), "jdbcDataRetrieverTest.xlsx", contextParams);
			
//			Assert.assertTrue(list.size() == 7);
//			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.NAME).equals("lineId"));
//			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.RENDER_TYPE).equals("select"));
//			Assert.assertTrue(StringUtils.hasText(list.get("商业产品线").get(PARAM_CONFIG.FETCH_URL)));
//			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.DEPENDENCIES).equals("lineId"));
//			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.FETCH_URL).equals("DUMMYexample/ind"));
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			File tmpImage = File.createTempFile("jdbcDataRetrieverTest.xlsx" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
