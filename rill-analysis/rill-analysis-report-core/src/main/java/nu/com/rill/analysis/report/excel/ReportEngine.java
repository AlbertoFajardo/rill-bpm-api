package nu.com.rill.analysis.report.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.rill.bpm.api.WorkflowOperations;
import org.springframework.beans.factory.ListableBeanFactory;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.access.SingletonBeanFactoryLocator;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.ExcelImporter;

/**
 * Excel report engine. 
 * @author mengran
 *
 */
public final class ReportEngine {

	public static final String _SETTINGS_SHEET = "_settings";
	public static final String _INPUT_SHEET = "_input";
	public static final char FULL_LENGTH_QUESTING = '\uFF1F';
	
	public static final String REPORT_PARAMETERS = "REPORT_PARAMETERS";
	public static final String REPORT_SESSION = "REPORT_SESSION";
	public static final String REPORT_QUERYNAME = "REPORT_QUERYNAME";
	public static final String REPORT_DISCOVERY = "REPORT_DISCOVERY";
	public static final String REPORT_CUBE = "REPORT_CUBE";
	
	public static final String REPORT_SCHEDULE_MODE = "REPORT_SCHEDULE_MODE";
	// FIXME: MENGRAN. time dimension last level is?
	public static final String REPORT_SCHEDULE_FORMAT = "[\"Time\"]\\.[yyyy]\\.[\"Q\"q]\\.[mm]\\.[dd]";
	
	public static final String JSESSIONID = "JSESSIONID";
	public static final String RELOAD = "RELOAD";
	public static final String DATA_TYPE = "DATA_TYPE";
	public static final String USERNAME = "USERNAME";
	public static final String PASSWORD = "PASSWORD";
	public static final String URL = "URL";
	
	public final Log LOG = LogFactory.getLog(this.getClass());
	
	public static ObjectMapper mapper = new ObjectMapper();
	
	public static final ReportEngine INSTANCE = new ReportEngine();
	
	private Map<String, DataRetriever> drMap = null;
	private ListableBeanFactory reportEngneBeanfactory; 
	private ReportEngine() {
		// Singleton
		BeanFactoryReference bfr = SingletonBeanFactoryLocator.getInstance().useBeanFactory(this.getClass().getSimpleName());
		reportEngneBeanfactory = (ListableBeanFactory) bfr.getFactory();
		drMap = reportEngneBeanfactory.getBeansOfType(DataRetriever.class);
	}
	
	// API -------------------
	public Map<String, Map<PARAM_CONFIG, String>> retrieveReportParams(InputStream is, String bookName) {
		
		Assert.notNull(is);
		Assert.notNull(bookName);
		
		Book book = new ExcelImporter().imports(is, bookName);
		// 1. Validate
		boolean isValid = validateReportTemplate(book);
		
		if (isValid) {
			// 2. Handle #_SETTINGS_SHEET
			Map<String, Map<PARAM_CONFIG, String>> reportParams = retrieveReportParamsFromSettingsSheet(book.getWorksheet(_SETTINGS_SHEET));
			
			return reportParams;
		}
		
		return Collections.emptyMap();
	}
	
	public Workbook generateReportTemplate(InputStream is, String[][] data, int[][] dataType, String bookName) {
		
		Assert.notNull(is);
		Assert.notNull(bookName);
		
		Book book = new ExcelImporter().imports(is, bookName);
		
		try {
			// 1. Validate
			boolean isValid = validateReportTemplate(book);
			
			if (isValid) {
				
				// 3. Handle #_INPUT_SHEET sheet
				generateInput(book.getWorksheet(_INPUT_SHEET), data, dataType);
				
				// 4. Formula evaluate
//				bookAfterProcess.setForceFormulaRecalculation(true);
				book.getCreationHelper().createFormulaEvaluator().evaluateAll();
			}
			
			// 5. Clone book
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			book.write(baos);
			Workbook bookAfterProcess = new ExcelImporter().imports(new ByteArrayInputStream(baos.toByteArray()), bookName);
			
			return bookAfterProcess;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.warn("Ignore exception of is.close. " + bookName, e);
			}
		}
	}

	public Workbook generateReport(InputStream is, String bookName, Map<String, String> reportParams) {
		
		Assert.notNull(is);
		Assert.notNull(bookName);
		
		Map<String, String> useReportParams = new HashMap<String, String>();
		if (!CollectionUtils.isEmpty(reportParams)) {
			useReportParams.putAll(reportParams);
		}
		
		Book book = new ExcelImporter().imports(is, bookName);
		
		try {
			// 1. Validate
			boolean isValid = validateReportTemplate(book);
			
			if (isValid) {
				// 2. Handle #_SETTINGS_SHEET
				processSettings(book.getWorksheet(_SETTINGS_SHEET), useReportParams);
				
				// 3. Handle #_INPUT_SHEET sheet
				processInput(book.getWorksheet(_INPUT_SHEET), useReportParams);
				
				// 4. Formula evaluate
//				bookAfterProcess.setForceFormulaRecalculation(true);
				book.getCreationHelper().createFormulaEvaluator().evaluateAll();
				
				// 5. Processor invoke
				Map<String, Object> decorators = reportEngneBeanfactory.getBeansWithAnnotation(BookDecorator.class);
				for (Entry<String, Object> entry : decorators.entrySet()) {
					LOG.info("Invoke decorator " + entry.getKey());
//					AnnotationUtils.
//					ReflectionUtils.findMethod(entry.getClass(), name, paramTypes);
//					ReflectionUtils.invokeMethod(, target, args)
				}
			}
			
			// 5. Clone book
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			book.write(baos);
			Workbook bookAfterProcess = new ExcelImporter().imports(new ByteArrayInputStream(baos.toByteArray()), bookName);
			
			return bookAfterProcess;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		} finally {
			try {
				is.close();
			} catch (IOException e) {
				LOG.warn("Ignore exception of is.close. " + bookName, e);
			}
		}
		
	}
	// API ------------------- END
	public enum PARAM_CONFIG {
		VALUE, RENDER_TYPE, DEPENDENCIES, FETCH_URL
	}
	
	private Map<String, Map<PARAM_CONFIG, String>> retrieveReportParamsFromSettingsSheet(Worksheet settingsSheet) {
		
		// 2. Handle #_SETTINGS_SHEET
		Map<String, Map<PARAM_CONFIG, String>> reportParams = new LinkedHashMap<String, Map<PARAM_CONFIG, String>>(2);
		for (Row row : settingsSheet) {
			if (row.getLastCellNum() > 4 && !"".equals(row.getCell(3).getStringCellValue())) {
				Map<PARAM_CONFIG, String> paramConfig = new HashMap<PARAM_CONFIG, String>(4);
				for (PARAM_CONFIG pc : PARAM_CONFIG.values()) {
					int i = pc.ordinal() + 4;
					if (i < row.getLastCellNum()) {
						paramConfig.put(pc, row.getCell(i).getStringCellValue());
					}
				}
				reportParams.put(row.getCell(3).getStringCellValue(), paramConfig);
			}
		}
		return reportParams;
	}
	
	private void generateInput(Worksheet worksheet, String[][] data, int[][] dataType) {
		
		CellStyle stringCellStyle = worksheet.getRow(1).getCell(0).getCellStyle();
		CellStyle numberCellStyle = worksheet.getRow(1).getCell(1).getCellStyle();
		
		int rowIndex = -1;
		for (String[] element : data) {
			rowIndex++;
			Row row = worksheet.createRow(rowIndex);
			if (element.length == 0) {
				// Means blank
			} else if (element.length == 1 && element[0].startsWith("SELECT")) {
				// Means MDX
				Cell cell = row.createCell(0, Cell.CELL_TYPE_STRING);
				cell.setCellValue(element[0]);
			} else {
				// Means result
				for (int i = 0; i < element.length; i++) {
					String d = element[i];
					Cell cell = row.createCell(i, dataType[rowIndex][i]);
					switch (dataType[rowIndex][i]) {
						case Cell.CELL_TYPE_NUMERIC : 
							cell.setCellValue(new Double(d));
							cell.setCellStyle(numberCellStyle);
							break;
						case Cell.CELL_TYPE_STRING : 
							cell.setCellValue(d);
							cell.setCellStyle(stringCellStyle);
							break;
					}
				}
			}
		}
	}
	
	protected void processInput(Worksheet inputSheet, Map<String, String> reportParams) throws Exception {
		
		// 1. Check reload
		if (!new Boolean(true).toString().equals(reportParams.get(RELOAD))) {
			LOG.info("Disable reload feature: " + reportParams);
			return; 
		}
		
		for (Entry<String, DataRetriever> entry : drMap.entrySet()) {
			if (entry.getValue().supportType(nu.com.rill.analysis.report.excel.DataRetriever.DATA_TYPE.valueOf(reportParams.get(DATA_TYPE)))) {
				LOG.debug("Use data retriever: " + entry.getKey());
				entry.getValue().retrieveData(inputSheet, reportParams);
				return;
			}
		}
		
		LOG.warn("Do nothing because unknown data type: " + reportParams.get(DATA_TYPE));
		return; 
		
	}
	
	protected void processSettings(Worksheet settingsSheet, Map<String, String> reportParams) {
		
		String url = null, username = null, password = null, type = "mdx";
		Boolean reload = new Boolean(true);
		for (Row row : settingsSheet) {
			if ("url".equals(row.getCell(0).getStringCellValue())) {
				url = row.getCell(1).getStringCellValue();
			}
			if ("username".equals(row.getCell(0).getStringCellValue())) {
				username = row.getCell(1).getStringCellValue();
			}
			if ("password".equals(row.getCell(0).getStringCellValue())) {
				password = row.getCell(1).getStringCellValue();
			}
			if (url != null && username != null && password != null) {
				LOG.info("Retrieve datesource settings: " + url + " " + username + " " + password);
			}
			if ("reload".equals(row.getCell(0).getStringCellValue())) {
				reload = row.getCell(1).getBooleanCellValue();
			}
			if ("dataType".equals(row.getCell(0).getStringCellValue())) {
				type = row.getCell(1).getStringCellValue();
			}
		}
		reportParams.put(RELOAD, reload.toString());
		reportParams.put(URL, url);
		reportParams.put(DATA_TYPE, type);
		
		// Report parameter's value is high-priority
		if (reportParams.containsKey(USERNAME) && reportParams.get(USERNAME) != null && !reportParams.get(USERNAME).equals(username)) {
			reportParams.put(USERNAME, username);
		}
		if (reportParams.containsKey(PASSWORD) && reportParams.get(PASSWORD) != null && !reportParams.get(PASSWORD).equals(username)) {
			reportParams.put(PASSWORD, password);
		}
		
		Map<String, Map<PARAM_CONFIG, String>> parameters = retrieveReportParamsFromSettingsSheet(settingsSheet);
		if (!CollectionUtils.isEmpty(parameters)) {
			reportParams.put(REPORT_PARAMETERS, WorkflowOperations.XStreamSerializeHelper.serializeXml(REPORT_PARAMETERS, parameters));
			LOG.info("Have added " + REPORT_PARAMETERS + " to report params: " + reportParams);
		}
		
	}
	
	protected boolean validateReportTemplate(Book book) {
		
		Worksheet sheet = book.getWorksheet(_SETTINGS_SHEET);
		if (sheet == null) {
			LOG.info(book.getBookName() + " not contains sheet: " + _SETTINGS_SHEET);
			return false;
		}
		sheet = book.getWorksheet(_INPUT_SHEET);
		if (sheet == null) {
			LOG.info(book.getBookName() + " not contains sheet: " + _INPUT_SHEET);
			return false;
		}
		
		if (!(book instanceof XSSFWorkbook)) {
			throw new UnsupportedOperationException("Only support Excel 2007~. " + book.getBookName());
		}
		
		return true;
	}
}
