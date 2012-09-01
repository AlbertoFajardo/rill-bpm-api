package nu.com.rill.analysis.report.excel.data;

import java.util.Iterator;
import java.util.Map;
import java.util.Random;

import nu.com.rill.analysis.report.excel.DataRetriever;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.lang.time.DateUtils;
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
				dataSheet.getRow(originalLastRowNum).getCell(1).setCellValue(1944111);
				for (int i = 0; i < 20; i++) {
					Row newRow = ReportEngine.copyRow(dataSheet, dataSheet.getLastRowNum(), dataSheet.getLastRowNum() + 1);
					Integer randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
					newRow.getCell(0).setCellValue(DateUtils.addDays(newRow.getCell(0).getDateCellValue(), 1));
					newRow.getCell(1).setCellValue(randomNum.doubleValue());
				}
				originalLastRowNum = dataSheet.getLastRowNum();
				int originalLastColNum = dataSheet.getRow(originalLastRowNum).getLastCellNum();
				for (int index = 0; index < 3; index++) {
					// Create new series
					for (int i = 3; i <= originalLastRowNum; i++) {
						Integer randomNum = new Random().nextInt(4344110 - 2444111 + 1) + 2444111;
						Row currentRow = dataSheet.getRow(i);
						Cell c = currentRow.createCell(originalLastColNum);
						if (i == 3) {
							c.setCellValue("高级经理" + randomNum);
						} else {
							c.setCellValue(randomNum);
						}
					}
					originalLastColNum++;
				}
			}
			if ("新客户数".equals(reportParams.get("分析指标"))) {
				int originalLastRowNum = dataSheet.getLastRowNum();
				dataSheet.getRow(originalLastRowNum).getCell(dataSheet.getRow(originalLastRowNum).getLastCellNum()).setCellValue(121);
				for (int i = 0; i < 20; i++) {
					Row newRow = ReportEngine.copyRow(dataSheet, originalLastRowNum, originalLastRowNum + 1);
					Integer randomNum = new Random().nextInt(200 - 100 + 1) + 100;
					newRow.getCell(0).setCellValue(DateUtils.addDays(newRow.getCell(0).getDateCellValue(), 1));
					newRow.getCell(1).setCellValue(randomNum.doubleValue());
				}
				originalLastRowNum = dataSheet.getLastRowNum();
				int originalLastColNum = dataSheet.getRow(originalLastRowNum).getLastCellNum();
				for (int index = 0; index < 3; index++) {
					originalLastColNum++;
					// Create new series
					for (int i = 3; i < originalLastRowNum; i++) {
						Integer randomNum = new Random().nextInt(200 - 100 + 1) + 100;
						Row currentRow = dataSheet.getRow(i);
						Cell c = currentRow.createCell(originalLastColNum);
						if (i == 3) {
							c.setCellValue("高级经理" + randomNum);
						} else {
							c.setCellValue(randomNum);
						}
					}
				}
			}
		} else if ("data_row.action".equals(dataSheet.getRow(dataSheet.getFirstRowNum()).getCell(0).getStringCellValue())) {
			dataSheet.getRow(1).getCell(1).setCellValue(reportParams.get("商业产品线"));
			dataSheet.getRow(2).getCell(1).setCellValue(reportParams.get("分析指标"));
			if ("点击消费".equals(reportParams.get("分析指标"))) {
				int originalLastRowNum = dataSheet.getLastRowNum();
				dataSheet.getRow(originalLastRowNum).getCell(2).setCellValue(1944111);
				for (int i = 0; i < 15; i++) {
					int lcn = dataSheet.getRow(3).getLastCellNum();
					dataSheet.getRow(3).createCell(lcn).setCellValue(DateUtils.addDays(dataSheet.getRow(3).getCell(lcn - 1).getDateCellValue(), 1));
					dataSheet.getRow(3).getCell(lcn).setCellStyle(dataSheet.getRow(3).getCell(lcn - 1).getCellStyle());
					Integer randomNum = new Random().nextInt(3344110 - 2344110 + 1) + 2344110;
					dataSheet.getRow(4).createCell(lcn).setCellValue(randomNum);
				}
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
		
		if ("pivottable.action".equals(dataSheet.getRow(dataSheet.getFirstRowNum()).getCell(0).getStringCellValue())) {
			
			Cell newInd = dataSheet.getRow(4).createCell(5);
			Cell newIndData = dataSheet.getRow(5).createCell(5);
			newInd.setCellValue("新客户数项");
			newInd.setCellStyle(dataSheet.getRow(4).getCell(2).getCellStyle());
			newIndData.setCellValue(101);
			newIndData.setCellStyle(dataSheet.getRow(5).getCell(2).getCellStyle());
			Cell newIndData6 = dataSheet.getRow(6).createCell(5);
			Cell newIndData7 = dataSheet.getRow(7).createCell(5);
			newIndData6.setCellStyle(dataSheet.getRow(5).getCell(2).getCellStyle());
			newIndData7.setCellStyle(dataSheet.getRow(5).getCell(2).getCellStyle());
			newIndData6.setCellValue(102);
			newIndData7.setCellValue(99);
			
			Cell newIndHuanbi = dataSheet.getRow(4).createCell(6);
			Cell newIndHuanbiData = dataSheet.getRow(5).createCell(6);
			newIndHuanbi.setCellValue("新客户数项H");
			newIndHuanbi.setCellStyle(dataSheet.getRow(4).getCell(3).getCellStyle());
			newIndHuanbiData.setCellValue(11);
			newIndHuanbiData.setCellStyle(dataSheet.getRow(5).getCell(3).getCellStyle());
			Cell newIndHuanbiData6 = dataSheet.getRow(6).createCell(6);
			Cell newIndHuanbiData7 = dataSheet.getRow(7).createCell(6);
			newIndHuanbiData6.setCellStyle(dataSheet.getRow(5).getCell(3).getCellStyle());
			newIndHuanbiData7.setCellStyle(dataSheet.getRow(5).getCell(3).getCellStyle());
			newIndHuanbiData6.setCellValue(12);
			newIndHuanbiData7.setCellValue(13);
			
			Cell newIndTongbi = dataSheet.getRow(4).createCell(7);
			Cell newIndTongbiData = dataSheet.getRow(5).createCell(7);
			newIndTongbi.setCellValue("新客户数项T");
			newIndTongbi.setCellStyle(dataSheet.getRow(4).getCell(4).getCellStyle());
			newIndTongbiData.setCellValue(12);
			newIndTongbiData.setCellStyle(dataSheet.getRow(5).getCell(4).getCellStyle());
			Cell newIndTongbiData6 = dataSheet.getRow(6).createCell(7);
			Cell newIndTongbiData7 = dataSheet.getRow(7).createCell(7);
			newIndTongbiData6.setCellStyle(dataSheet.getRow(5).getCell(4).getCellStyle());
			newIndTongbiData7.setCellStyle(dataSheet.getRow(5).getCell(4).getCellStyle());
			newIndTongbiData6.setCellValue(21);
			newIndTongbiData7.setCellValue(21);
			
			int originalLastRowNum = dataSheet.getLastRowNum();
			Row newRow = ReportEngine.copyRow(dataSheet, originalLastRowNum, originalLastRowNum + 1);
			for (Iterator<Cell> it = newRow.cellIterator(); it.hasNext();) {
				Cell c = it.next();
				Integer randomNum = new Random().nextInt(100 - 50 + 1) + 50;
				if (c.getColumnIndex() == 0) {
					c.setCellValue("高级经理");
				} else if (c.getColumnIndex() == 1) {
					c.setCellValue("经理" + randomNum);
				} else {
					c.setCellValue(randomNum);
				}
			}
		}
		
		if ("table.action".equals(dataSheet.getRow(dataSheet.getFirstRowNum()).getCell(0).getStringCellValue())) {
			
			Cell newInd = dataSheet.getRow(4).createCell(5);
			Cell newIndData = dataSheet.getRow(5).createCell(5);
			newInd.setCellValue("新客户数项");
			newInd.setCellStyle(dataSheet.getRow(4).getCell(2).getCellStyle());
			newIndData.setCellValue(101);
			newIndData.setCellStyle(dataSheet.getRow(5).getCell(2).getCellStyle());
			Cell newIndData6 = dataSheet.getRow(6).createCell(5);
			Cell newIndData7 = dataSheet.getRow(7).createCell(5);
			newIndData6.setCellStyle(dataSheet.getRow(5).getCell(2).getCellStyle());
			newIndData7.setCellStyle(dataSheet.getRow(5).getCell(2).getCellStyle());
			newIndData6.setCellValue(102);
			newIndData7.setCellValue(99);
			
			Cell newIndHuanbi = dataSheet.getRow(4).createCell(6);
			Cell newIndHuanbiData = dataSheet.getRow(5).createCell(6);
			newIndHuanbi.setCellValue("新客户数项H");
			newIndHuanbi.setCellStyle(dataSheet.getRow(4).getCell(3).getCellStyle());
			newIndHuanbiData.setCellValue(11);
			newIndHuanbiData.setCellStyle(dataSheet.getRow(5).getCell(3).getCellStyle());
			Cell newIndHuanbiData6 = dataSheet.getRow(6).createCell(6);
			Cell newIndHuanbiData7 = dataSheet.getRow(7).createCell(6);
			newIndHuanbiData6.setCellStyle(dataSheet.getRow(5).getCell(3).getCellStyle());
			newIndHuanbiData7.setCellStyle(dataSheet.getRow(5).getCell(3).getCellStyle());
			newIndHuanbiData6.setCellValue(12);
			newIndHuanbiData7.setCellValue(13);
			
			Cell newIndTongbi = dataSheet.getRow(4).createCell(7);
			Cell newIndTongbiData = dataSheet.getRow(5).createCell(7);
			newIndTongbi.setCellValue("新客户数项T");
			newIndTongbi.setCellStyle(dataSheet.getRow(4).getCell(4).getCellStyle());
			newIndTongbiData.setCellValue(12);
			newIndTongbiData.setCellStyle(dataSheet.getRow(5).getCell(4).getCellStyle());
			Cell newIndTongbiData6 = dataSheet.getRow(6).createCell(7);
			Cell newIndTongbiData7 = dataSheet.getRow(7).createCell(7);
			newIndTongbiData6.setCellStyle(dataSheet.getRow(5).getCell(4).getCellStyle());
			newIndTongbiData7.setCellStyle(dataSheet.getRow(5).getCell(4).getCellStyle());
			newIndTongbiData6.setCellValue(21);
			newIndTongbiData7.setCellValue(21);
			
			int originalLastRowNum = dataSheet.getLastRowNum();
			Row newRow = ReportEngine.copyRow(dataSheet, originalLastRowNum, originalLastRowNum + 1);
			for (Iterator<Cell> it = newRow.cellIterator(); it.hasNext();) {
				Cell c = it.next();
				Integer randomNum = new Random().nextInt(100 - 50 + 1) + 50;
				if (c.getColumnIndex() == 0) {
					c.setCellValue("高级经理");
				} else if (c.getColumnIndex() == 1) {
					c.setCellValue("经理" + randomNum);
				} else {
					c.setCellValue(randomNum);
				}
			}
		}
		
		return;
	}

}
