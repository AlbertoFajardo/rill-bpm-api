package nu.com.rill.analysis.report.excel;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;

import org.apache.commons.io.FileUtils;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.zkoss.lang.Library;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.ExcelImporter;

public class SmartChartTest {

	@Test
	public void testProcess() {
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan.xlsx");
		
		try {
			Library.setProperty("org.zkoss.zss.model.impl.SheetCtrl.class", "org.zkoss.zssex.model.impl.SheetCtrlImpl");
			Book book = new ExcelImporter().imports(cpr.getInputStream(), "luopan.xlsx");
			ChartProcessor sc = new ChartProcessor();
			sc.process(book);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			book.write(baos);
			File tmpImage = File.createTempFile("luopan.xlsx" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		} finally {
			try {
				cpr.getInputStream().close();
			} catch (IOException e) {
				// Ignore
			}
		}
	}
}
