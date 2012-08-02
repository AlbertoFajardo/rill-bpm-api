package nu.com.rill.analysis.report.excel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.zkoss.poi.ss.usermodel.Workbook;

public class ReportEngineTests {

	@Test
	public void generateReport() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		Map<String, String> reportParams = new HashMap<String, String>();
		reportParams.put("[Time].[2011]", "[Time].[2010]");
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/Report Desinger_20120715.xlsx");
		try {
			Workbook wb = re.generateReport(cpr.getInputStream(), "Report Desinger_20120715.xlsx", reportParams);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			File tmpImage = File.createTempFile("Report Desinger_" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void retrieveReportParams() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/Report Desinger_20120715.xlsx");
		try {
			Map<String, String> list = re.retrieveReportParams(cpr.getInputStream(), "Report Desinger_20120715.xlsx");
			Assert.assertTrue(list.size() == 1);
			Assert.assertTrue(list.get("时间").equals("[Time].[2011]"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}

}
