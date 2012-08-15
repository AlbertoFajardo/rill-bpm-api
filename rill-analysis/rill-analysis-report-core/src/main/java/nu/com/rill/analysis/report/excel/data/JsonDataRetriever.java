package nu.com.rill.analysis.report.excel.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import nu.com.rill.analysis.report.excel.DataRetriever;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.zss.model.Worksheet;

public class JsonDataRetriever implements DataRetriever {

	@Override
	public boolean supportType(DATA_TYPE dt) {
		
		return DATA_TYPE.json.equals(dt);
	}

	@Override
	public void retrieveData(Worksheet dataSheet, Map<String, String> reportParams) {
		
		// FIXME: Need implement for real world.
		// Retrieve JSON data from URL and store into sheet.
		if ("data.action".equals(dataSheet.getRow(dataSheet.getFirstRowNum()).getCell(0).getStringCellValue())) {
			dataSheet.getRow(1).getCell(1).setCellValue(reportParams.get("商业产品线"));
			dataSheet.getRow(2).getCell(1).setCellValue(reportParams.get("分析指标"));
			if ("点击消费".equals(reportParams.get("分析指标"))) {
				int originalLastRowNum = dataSheet.getLastRowNum();
				Row newRow = ReportEngine.copyRow(dataSheet, originalLastRowNum, originalLastRowNum + 1);
				for (Iterator<Cell> it = newRow.cellIterator(); it.hasNext();) {
					Cell c = it.next();
					Integer randomNum = new Random().nextInt(3344110 - 2344110 + 1) + 2344110;
					if (c.getColumnIndex() != newRow.getFirstCellNum()) {
						c.setCellValue(randomNum.longValue());
					} else {
						c.setCellValue("高级经理A");
					}
				}
				originalLastRowNum = dataSheet.getLastRowNum();
				newRow = ReportEngine.copyRow(dataSheet, originalLastRowNum, originalLastRowNum + 1);
				for (Iterator<Cell> it = newRow.cellIterator(); it.hasNext();) {
					Cell c = it.next();
					Integer randomNum = new Random().nextInt(3344110 - 2344110 + 1) + 2344110;
					if (c.getColumnIndex() != newRow.getFirstCellNum()) {
						c.setCellValue(randomNum.longValue());
					} else {
						c.setCellValue("高级经理B");
					}
				}
				originalLastRowNum = dataSheet.getLastRowNum();
				newRow = ReportEngine.copyRow(dataSheet, originalLastRowNum, originalLastRowNum + 1);
				for (Iterator<Cell> it = newRow.cellIterator(); it.hasNext();) {
					Cell c = it.next();
					Integer randomNum = new Random().nextInt(3344110 - 2344110 + 1) + 2344110;
					if (c.getColumnIndex() != newRow.getFirstCellNum()) {
						c.setCellValue(randomNum.longValue());
					} else {
						c.setCellValue("高级经理C");
					}
				}
			}
			if ("新客户数".equals(reportParams.get("分析指标"))) {
				int originalLastRowNum = dataSheet.getLastRowNum();
				for (Iterator<Cell> it = dataSheet.getRow(originalLastRowNum).cellIterator(); it.hasNext();) {
					Cell c = it.next();
					Integer randomNum = new Random().nextInt(100 - 50 + 1) + 50;
					if (c.getColumnIndex() != dataSheet.getRow(originalLastRowNum).getFirstCellNum()) {
						c.setCellValue(randomNum.longValue());
					}
				}
				Row newRow = ReportEngine.copyRow(dataSheet, originalLastRowNum, originalLastRowNum + 1);
				for (Iterator<Cell> it = newRow.cellIterator(); it.hasNext();) {
					Cell c = it.next();
					Integer randomNum = new Random().nextInt(100 - 50 + 1) + 50;
					if (c.getColumnIndex() != newRow.getFirstCellNum()) {
						c.setCellValue(randomNum.longValue());
					} else {
						c.setCellValue("高级经理B");
					}
				}
			}
		}
		
		return;
	}

}
