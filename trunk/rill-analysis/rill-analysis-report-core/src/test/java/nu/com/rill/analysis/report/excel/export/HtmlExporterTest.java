package nu.com.rill.analysis.report.excel.export;

import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.ExcelImporter;

public class HtmlExporterTest {

	@Test
	public void export() {
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/htmlExporter.xlsx");
		try {
			Book book = new ExcelImporter().imports(cpr.getInputStream(), "htmlExporter.xlsx");
			String html = new HtmlExporter(book).export();
			
			File tmpImage = File.createTempFile("htmlExporter" + System.currentTimeMillis(), ".html");
			FileUtils.writeByteArrayToFile(tmpImage, html.getBytes("utf-8"));
//			Map<String, String> contextParams = new HashMap<String, String>();
//			contextParams.put(ReportEngine.URL, "DUMMY");
//			Map<String, Map<PARAM_CONFIG, String>> list = re.retrieveReportParams(cpr.getInputStream(), "luopan.xlsx", contextParams);
//			Assert.assertTrue(list.size() == 7);
//			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.NAME).equals("lineId"));
//			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.RENDER_TYPE).equals("select"));
//			Assert.assertTrue(StringUtils.hasText(list.get("商业产品线").get(PARAM_CONFIG.FETCH_URL)));
//			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.DEPENDENCIES).equals("lineId"));
//			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.FETCH_URL).equals("DUMMYexample/ind"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
