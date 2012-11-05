package nu.com.rill.analysis.report.excel.export;

import java.io.File;
import java.io.IOException;
import java.util.Map.Entry;

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
			HtmlExporter exporter = new HtmlExporter(book, "htmlExporter.xlsx");
			String html = exporter.export();
			
			// Generate image file
			for (Entry<String, byte[]> entry : exporter.getImages().entrySet()) {
				File tmpImage = new File(System.getProperty("java.io.tmpdir"), entry.getKey());
				FileUtils.writeByteArrayToFile(tmpImage, entry.getValue());
			}
			
			File tmpHtml = File.createTempFile("htmlExporter" + System.currentTimeMillis(), ".html");
			FileUtils.writeByteArrayToFile(tmpHtml, html.getBytes("utf-8"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
