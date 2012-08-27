package nu.com.rill.analysis.report.excel;

import java.io.File;
import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

import org.apache.commons.io.FileUtils;
import org.apache.commons.io.output.ByteArrayOutputStream;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.zkoss.lang.Library;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.impl.ExcelImporter;

public class ReportEngineTests {

	@Test
	public void generateReportSaiku6() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		Map<String, String> reportParams = new HashMap<String, String>();
		reportParams.put("[Time].[2011]", "[Time].[2010]");
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/saiku-export (6).xlsx");
		try {
			Workbook wb = re.generateReport(cpr.getInputStream(), "saiku-export (6).xlsx", reportParams);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			File tmpImage = File.createTempFile("saiku-export (6).xlsx_" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void generateReportDemo() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		Map<String, String> reportParams = new HashMap<String, String>();
		reportParams.put("时间", "2010-08-12");
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/demo.xlsx");
		try {
			Workbook wb = re.generateReport(cpr.getInputStream(), "demo.xlsx", reportParams);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			File tmpImage = File.createTempFile("demo.xlsx_" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
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
	public void generateReportLuopan() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		Map<String, String> reportParams = new HashMap<String, String>();
		reportParams.put("商业产品线", "网盟");
		reportParams.put("分析指标", "点击消费");
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan.xlsx");
		try {
			Workbook wb = re.generateReport(cpr.getInputStream(), "luopan.xlsx", reportParams);
			Assert.assertEquals("网盟", wb.getSheet(ReportEngine._INPUT_SHEET).getRow(1).getCell(1).getStringCellValue());
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			File tmpImage = File.createTempFile("luopan.xlsx_" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void generateReportLuopan_Pivottable() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		Map<String, String> reportParams = new HashMap<String, String>();
		reportParams.put("商业产品线", "网盟");
		reportParams.put("分析指标", "点击消费");
		
		Library.setProperty("org.zkoss.poi.ss.usermodel.PivotTableHelper.class", "org.zkoss.zpoiex.ss.usermodel.helpers.PivotTableHelper");
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan_pivottable.xlsx");
		try {
			Workbook wb = re.generateReport(cpr.getInputStream(), "luopan_pivottable.xlsx", reportParams);
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			File tmpImage = File.createTempFile("luopan_pivottable.xlsx_" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void generateReportLuopan_Pivottable2() {
		
		Map<String, String> reportParams = new HashMap<String, String>();
		reportParams.put("商业产品线", "网盟");
		reportParams.put("分析指标", "点击消费");
		
		Library.setProperty("org.zkoss.poi.ss.usermodel.PivotTableHelper.class", "org.zkoss.zpoiex.ss.usermodel.helpers.PivotTableHelper");
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan_pivottable2.xlsx");
		try {
			Book book = new ExcelImporter().imports(cpr.getInputStream(), "luopan_pivottable2.xlsx");
			
			book.getCreationHelper().createFormulaEvaluator().evaluateAll();
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			book.write(baos);
			File tmpImage = File.createTempFile("luopan_pivottable2.xlsx_" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void generateReportLuopan_table() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		Map<String, String> reportParams = new HashMap<String, String>();
		reportParams.put("商业产品线", "网盟");
		reportParams.put("分析指标", "点击消费");
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan_table.xlsx");
		try {
			Workbook wb = re.generateReport(cpr.getInputStream(), "luopan_table.xlsx", reportParams);
			Assert.assertEquals("网盟", wb.getSheet(ReportEngine._INPUT_SHEET).getRow(1).getCell(1).getStringCellValue());
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			wb.write(baos);
			File tmpImage = File.createTempFile("luopan_table.xlsx_" + System.currentTimeMillis(), ".xlsx");
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
			Map<String, Map<PARAM_CONFIG, String>> list = re.retrieveReportParams(cpr.getInputStream(), "Report Desinger_20120715.xlsx");
			Assert.assertTrue(list.size() == 1);
			Assert.assertTrue(list.get("时间").get(PARAM_CONFIG.VALUE).equals("[Time].[2011]"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void retrieveReportParamsLuopan() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan.xlsx");
		try {
			Map<String, Map<PARAM_CONFIG, String>> list = re.retrieveReportParams(cpr.getInputStream(), "luopan.xlsx");
			Assert.assertTrue(list.size() == 5);
			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.FETCH_URL).equals("line.action"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void generateReportTemplate() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/template.xlsx");
		try {
			String[][] data = new String[8][];
			int[][] dataType = new int[8][];
			// Prepare test data
			String[] mdx1 = new String[] {"SELECT {Hierarchize({[Measures].[Show Cnt], [Measures].[Click Cnt], [Measures].[Ask Cnt], [Measures].[Access Cnt], [Measures].[Order Cnt]})} ON COLUMNS,{Hierarchize({[Time].[2011]})} ON ROWS FROM [TF_CUBE]"};
			data[0] = mdx1;
			dataType[0] = new int[] {1};
			String[] data1 = new String[] {"Year", "展现量", "点击量", "访问量", "咨询量", "订单量"};
			data[1] = data1;
			dataType[1] = new int[] {1, 1, 1, 1, 1, 1};
			String[] data2 = new String[] {"2011", "1111111", "374839283", "21785448", "4793511", "134337"};
			data[2] = data2;
			dataType[2] = new int[] {1, 0, 0, 0, 0, 0};
			data[3] = new String[0];
			dataType[3] = new int[0];
			
			String[] mdx2 = new String[] {"SELECT {Hierarchize({[Measures].[Show Cnt], [Measures].[Click Cnt], [Measures].[Ask Cnt], [Measures].[Access Cnt], [Measures].[Order Cnt]})} ON COLUMNS,{Hierarchize({{[Time].[2011]}, Filter({{[Time].[Month].Members}}, (Exists(Ancestor([Time].CurrentMember, [Time].[Year]), {[Time].[2011]}).Count  > 0))})} ON ROWS FROM [TF_CUBE]"};
			data[4] = mdx2;
			dataType[4] = new int[] {1};
			String[] data5 = new String[] {"Year", "Month", "展现量", "点击量", "访问量", "咨询量", "订单量"};
			data[5] = data5;
			dataType[5] = new int[] {1, 1, 1, 1, 1, 1, 1};
			String[] data6 = new String[] {"2011", "201101", "56859890", "3421713", "1908244", "420420", "11849"};
			data[6] = data6;
			dataType[6] = new int[] {1, 1, 0, 0, 0, 0, 0};
			data[7] = new String[0];
			dataType[7] = new int[0];
			
			Workbook templateBook = re.generateReportTemplate(cpr.getInputStream(), data, dataType, "template.xlsx");
			
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			templateBook.write(baos);
			File tmpImage = File.createTempFile("template_" + System.currentTimeMillis(), ".xlsx");
			FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
