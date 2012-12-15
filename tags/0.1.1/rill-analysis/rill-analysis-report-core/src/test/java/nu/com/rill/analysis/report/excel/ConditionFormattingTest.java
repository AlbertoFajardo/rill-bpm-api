package nu.com.rill.analysis.report.excel;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Workbook;

public class ConditionFormattingTest {

	@Test
	public void conditionFormatting() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		Map<String, String> reportParams = new HashMap<String, String>();
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/ConditionFormatting.xlsx");
		try {
			Workbook wb = re.generateReport(cpr.getInputStream(), "ConditionFormatting.xlsx", reportParams);
			
			for (int i = 0; i <= wb.getSheetAt(0).getLastRowNum(); i++) {
				Row row = wb.getSheetAt(0).getRow(i);
				if (row == null) {
					continue;
				}
					
				for (int j = 0; j <= row.getLastCellNum(); j++) {
					ConditionalFormattingHelper.getCFInWorkbook(wb.getSheetAt(0), row.getCell(j));
				}
			}
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
