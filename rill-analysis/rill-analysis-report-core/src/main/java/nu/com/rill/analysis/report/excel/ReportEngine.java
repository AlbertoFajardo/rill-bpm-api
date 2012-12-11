package nu.com.rill.analysis.report.excel;

import java.io.ByteArrayInputStream;
import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.io.InputStream;
import java.net.URLEncoder;
import java.util.ArrayList;
import java.util.Collections;
import java.util.HashMap;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import javax.servlet.http.HttpServletResponse;

import nu.com.rill.analysis.report.REException;

import org.apache.commons.httpclient.HttpClient;
import org.apache.commons.httpclient.NameValuePair;
import org.apache.commons.httpclient.methods.PostMethod;
import org.apache.commons.io.IOUtils;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.ObjectMapper;
import org.rill.bpm.api.WorkflowOperations;
import org.springframework.beans.factory.access.BeanFactoryReference;
import org.springframework.beans.factory.config.ConfigurableListableBeanFactory;
import org.springframework.context.access.ContextSingletonBeanFactoryLocator;
import org.springframework.context.support.AbstractApplicationContext;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.CellStyle;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.ss.util.CellRangeAddress;
import org.zkoss.poi.ss.util.CellReference;
import org.zkoss.poi.xssf.usermodel.XSSFTable;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.ExcelImporter;
import org.zkoss.zss.model.impl.XSSFSheetImpl;

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
	public static final String COOKIE = "Cookie";
	
	public static final String SYSTEM_VIEW_PAGE = "re.mail.systemViewPage";
	
	public static final String PARAM_TABLE = "paramTable";
	
	public static final Log LOG = LogFactory.getLog(ReportEngine.class);
	
	public static ObjectMapper mapper = new ObjectMapper();
	private static HttpClient httpClient = null;
	
	public static final ReportEngine INSTANCE = new ReportEngine();
	
	private Map<String, DataRetriever> drMap = null;
	private ConfigurableListableBeanFactory reportEngneBeanfactory; 
	private ReportEngine() {
		// Singleton
		BeanFactoryReference bfr = ContextSingletonBeanFactoryLocator.getInstance().useBeanFactory(this.getClass().getSimpleName());
		reportEngneBeanfactory = ((AbstractApplicationContext) bfr.getFactory()).getBeanFactory();
		drMap = reportEngneBeanfactory.getBeansOfType(DataRetriever.class);
		httpClient = reportEngneBeanfactory.getBean(HttpClient.class);
		Assert.notNull(httpClient);
	}
	
	private static ThreadLocal<String> cookieHolder = new ThreadLocal<String>();
	public static void registCookie(String cookie) {
		if (cookie != null) {
			Assert.isNull(cookieHolder.get(), "Already regist cookie into thread? " + cookieHolder.get());
		}
		cookieHolder.set(cookie);
	}
	
	public static String retrieveCookie() {
		
		return cookieHolder.get();
	}
	
	public static String fetchUrl(String url , Map<String, String> params) throws REException {
		
		String prefix = "", suffix = "";
		String[] failOver = new String[] {url};
		if (url.contains("[") && url.contains("]")) {
			failOver = url.substring(url.indexOf("[") + 1, url.indexOf("]")).split(" ");
			prefix = url.substring(0, url.indexOf("["));
			suffix = url.substring(url.indexOf("]") + 1);
		}
		
		long startTime = System.currentTimeMillis();
		LOG.debug("Start fetchUrl " + url + " " + startTime);
		
		for (int i = 0 ; i < failOver.length; i++) {
			String f = failOver[i];
			String urlUse = prefix + f + suffix;
			try {
				PostMethod httppost = new PostMethod(urlUse);
				List<NameValuePair> formparams = new ArrayList<NameValuePair>();
				for (Entry<String, String> entry : params.entrySet()) {
					formparams.add(new NameValuePair(entry.getKey(), entry.getValue()));
				}
				
				try {
					httppost.addRequestHeader("Content-Type", PostMethod.FORM_URL_ENCODED_CONTENT_TYPE + ";charset=utf8");
					httppost.setRequestBody(formparams.toArray(new NameValuePair[0]));
					httppost.addRequestHeader("Cookie", retrieveCookie());
					httpClient.executeMethod(httppost);
					if (httppost.getStatusCode() != HttpServletResponse.SC_OK) {
						throw new IllegalStateException("无法正常访问" + "[" + httppost.getStatusCode() + "]: " + urlUse);
					}
//					if (httppost.getResponseHeader("Content-Type") != null && !httppost.getResponseHeader("Content-Type").getValue().contains("application/json")) {
//						throw new IllegalStateException("仅允许响应application/json数据: " + httppost.getResponseHeader("Content-Type").getValue());
//					}
					InputStream is = httppost.getResponseBodyAsStream();
					ByteArrayOutputStream baos = new ByteArrayOutputStream();
					IOUtils.copy(is, baos);
				    String content = new String(baos.toByteArray(), "UTF-8");
				    
				    LOG.debug("End fetchUrl " + url + " " + (System.currentTimeMillis() - startTime));
				    return content;
				} catch (IllegalStateException e) {
					LOG.error(e);
					throw new REException(e.getMessage(), e);
				} catch (IOException e) {
					LOG.error(e);
					throw new REException("连接拒绝" + "[" + urlUse + "].");
				} finally {
					httppost.releaseConnection();
				}
			} catch (REException e) {
				if (i != failOver.length - 1) {
					LOG.warn("Fail to fetch url, retry next one..." + urlUse, e);
				} else {
					throw e;
				}
			}
			
		}
		
		throw new REException("无法正常访问" + "[" + url + "].");
	}
	
	public static Row copyRow(Worksheet worksheet, int sourceRowNum, int destinationRowNum) {
        // Get the source / new row
        Row newRow = worksheet.getRow(destinationRowNum);
        Row sourceRow = worksheet.getRow(sourceRowNum);

        // If the row exist in destination, push down all rows by 1 else create a new row
        if (newRow != null) {
            worksheet.shiftRows(destinationRowNum, worksheet.getLastRowNum(), 1);
        } else {
            newRow = worksheet.createRow(destinationRowNum);
        }

        // Loop through source columns to add to new row
        for (int i = 0; i < sourceRow.getLastCellNum(); i++) {
            // Grab a copy of the old/new cell
            Cell oldCell = sourceRow.getCell(i);
            Cell newCell = newRow.createCell(i);

            // If the old cell is null jump to next cell
            if (oldCell == null) {
                newCell = null;
                continue;
            }

            // Copy style from old cell and apply to new cell
            CellStyle newCellStyle = worksheet.getWorkbook().createCellStyle();
            newCellStyle.cloneStyleFrom(oldCell.getCellStyle());
            newCell.setCellStyle(newCellStyle);

            // If there is a cell comment, copy
            if (newCell.getCellComment() != null) {
                newCell.setCellComment(oldCell.getCellComment());
            }

            // If there is a cell hyperlink, copy
            if (oldCell.getHyperlink() != null) {
                newCell.setHyperlink(oldCell.getHyperlink());
            }

            // Set the cell data type
            newCell.setCellType(oldCell.getCellType());

            // Set the cell data value
            switch (oldCell.getCellType()) {
                case Cell.CELL_TYPE_BLANK:
                    newCell.setCellValue(oldCell.getStringCellValue());
                    break;
                case Cell.CELL_TYPE_BOOLEAN:
                    newCell.setCellValue(oldCell.getBooleanCellValue());
                    break;
                case Cell.CELL_TYPE_ERROR:
                    newCell.setCellErrorValue(oldCell.getErrorCellValue());
                    break;
                case Cell.CELL_TYPE_FORMULA:
                    newCell.setCellFormula(oldCell.getCellFormula());
                    break;
                case Cell.CELL_TYPE_NUMERIC:
                    newCell.setCellValue(oldCell.getNumericCellValue());
                    break;
                case Cell.CELL_TYPE_STRING:
                    newCell.setCellValue(oldCell.getRichStringCellValue());
                    break;
            }
        }

		// If there are are any merged regions in the source row, copy to new row
		for (int i = 0; i < worksheet.getNumMergedRegions(); i++) {
			CellRangeAddress cellRangeAddress = worksheet.getMergedRegion(i);
			if (cellRangeAddress.getFirstRow() == sourceRow.getRowNum()) {
				CellRangeAddress newCellRangeAddress = new CellRangeAddress(
						newRow.getRowNum(),
						(newRow.getRowNum() + (cellRangeAddress.getLastRow() - cellRangeAddress
								.getFirstRow())), cellRangeAddress
								.getFirstColumn(), cellRangeAddress
								.getLastColumn());
				worksheet.addMergedRegion(newCellRangeAddress);
			}
		}
		
		return newRow;
	}
	
	// API -------------------
	public String generateReportViewUrl(InputStream is, String bookName, Map<String, String> contextParams, boolean combine) {
		
		 Map<String, Map<PARAM_CONFIG, String>> params = retrieveReportParams(is, bookName, contextParams, combine);
		 
		 return generateReportViewUrl(bookName, contextParams, params, combine);
	}
	
	public String generateReportViewUrl(String bookName, Map<String, String> contextParams, Map<String, Map<PARAM_CONFIG, String>> params, boolean combine) {
		
		 String systemViewPage = contextParams == null ? "" : contextParams.get(SYSTEM_VIEW_PAGE);
		 if (!StringUtils.hasText(systemViewPage)) {
			 systemViewPage = reportEngneBeanfactory.resolveEmbeddedValue("${" + SYSTEM_VIEW_PAGE + "}");
		 }
		 if (!StringUtils.hasText(systemViewPage)) {
			 return "";
		 }
		 
		 systemViewPage = systemViewPage.endsWith("?") ? systemViewPage : systemViewPage + "?";
		 systemViewPage = systemViewPage + "fileName=" + bookName;
		 
		 try {
			 StringBuilder sb = new StringBuilder();
			 for(Entry<String, Map<PARAM_CONFIG, String>> entry : params.entrySet()) {
				 if (entry.getValue().get(PARAM_CONFIG.RENDER_TYPE) != null) {
					 sb.append("&");
					 sb.append(entry.getValue().get(PARAM_CONFIG.NAME));
					 sb.append("=");
					 sb.append(URLEncoder.encode(entry.getValue().get(PARAM_CONFIG.VALUE), "utf-8"));
				 }
			 }
			 
			 return systemViewPage + sb.toString();
		 } catch (Exception e) {
			 LOG.warn("Error occurred when try to build system view page", e);
		 }
		 
		 return "";
	}
	
	public Map<String, Map<PARAM_CONFIG, String>> retrieveReportParams(InputStream is, String bookName) {
		
		Assert.notNull(is);
		Assert.notNull(bookName);
		
		return retrieveReportParams(is, bookName, new HashMap<String, String>(0));
	}
	
	public Map<String, Map<PARAM_CONFIG, String>> retrieveReportParams(InputStream is, String bookName, Map<String, String> contextParams, boolean combine) {
		
		Assert.notNull(is);
		Assert.notNull(bookName);
		
		Book book = new ExcelImporter().imports(is, bookName);
		// 1. Validate
		boolean isValid = validateReportTemplate(book);
		
		if (isValid) {
			return retrieveReportParams(book, contextParams, combine);
		}
		
		return Collections.emptyMap();
	}
	
	public Map<String, Map<PARAM_CONFIG, String>> retrieveReportParams(InputStream is, String bookName, Map<String, String> contextParams) {
		
		Assert.notNull(is);
		Assert.notNull(bookName);
		
		Book book = new ExcelImporter().imports(is, bookName);
		// 1. Validate
		boolean isValid = validateReportTemplate(book);
		
		if (isValid) {
			return retrieveReportParams(book, contextParams, false);
		}
		
		return Collections.emptyMap();
	}
	
	public Map<String, Map<PARAM_CONFIG, String>> retrieveReportParams(Book book, Map<String, String> contextParams) {
		
		return retrieveReportParams(book, contextParams, false);
	}
	
	public Map<String, Map<PARAM_CONFIG, String>> retrieveReportParams(Book book, Map<String, String> contextParams, boolean combine) {
		
		Assert.notNull(book);
		
		// 2. Handle #_SETTINGS_SHEET
		processSettings(book.getWorksheet(_SETTINGS_SHEET), contextParams, combine);
		@SuppressWarnings("unchecked")
		Map<String, Map<PARAM_CONFIG, String>> reportParams = WorkflowOperations.XStreamSerializeHelper.deserializeObject(contextParams.get(REPORT_PARAMETERS), REPORT_PARAMETERS, LinkedHashMap.class); 
		// Add Cookie pair
		Map<PARAM_CONFIG, String> cookieParams = new LinkedHashMap<ReportEngine.PARAM_CONFIG, String>(0);
		cookieParams.put(PARAM_CONFIG.VALUE, "");
		cookieParams.put(PARAM_CONFIG.NAME, COOKIE);
		reportParams.put(COOKIE, cookieParams);
		
		Map<PARAM_CONFIG, String> urlParams = new LinkedHashMap<ReportEngine.PARAM_CONFIG, String>(0);
		urlParams.put(PARAM_CONFIG.VALUE, contextParams.get(URL));
		urlParams.put(PARAM_CONFIG.NAME, URL);
		reportParams.put(URL, urlParams);
		
		return reportParams;
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
			throw new REException("无法生成报表模版.", e);
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
		
		long startTime = System.currentTimeMillis();
		LOG.debug("Start generateReport " + startTime);
		
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
				processSettings(book.getWorksheet(_SETTINGS_SHEET), useReportParams, true);
				
				// 3. Handle #_INPUT_SHEET sheet
				for (int i = 0 ; i < book.getNumberOfSheets(); i++) {
					if (book.getSheetAt(i).getSheetName().startsWith(_INPUT_SHEET)) {
						LOG.debug("Process input sheet " + book.getSheetAt(i).getSheetName());
						processInput(book.getWorksheetAt(i), useReportParams);
					}
				}
				
				// 4. Formula evaluate
//				bookAfterProcess.setForceFormulaRecalculation(true);
				book.getCreationHelper().createFormulaEvaluator().evaluateAll();
				
				// 5. Processor invoke
				Map<String, BookDecorator> decorators = reportEngneBeanfactory.getBeansOfType(BookDecorator.class);
				for (Entry<String, BookDecorator> entry : decorators.entrySet()) {
					LOG.info("Invoke decorator " + entry.getKey());
					entry.getValue().process(book);
				}
			}
			
			// 5. Clone book
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			book.write(baos);
			Workbook bookAfterProcess = new ExcelImporter().imports(new ByteArrayInputStream(baos.toByteArray()), bookName);
			
			return bookAfterProcess;
		
		} catch (REException e) {
			LOG.error(e);
			throw new REException("无法生成报表。", e);	
		} catch (Exception e) {
			LOG.error(e);
			throw new REException(e);
		} finally {
			LOG.debug("End generateReport " + (System.currentTimeMillis() - startTime));
			try {
				is.close();
			} catch (IOException e) {
				LOG.warn("Ignore exception of is.close. " + bookName, e);
			}
		}
		
	}
	// API ------------------- END
	public enum PARAM_CONFIG {
		NAME, VALUE, RENDER_TYPE, DEPENDENCIES, FETCH_URL, FORMAT
	}
	
	private Map<String, Map<PARAM_CONFIG, String>> retrieveReportParamsFromSettingsSheet(Worksheet settingsSheet, Map<String, String> contextParams, boolean combine) {
		
		// 2. Handle #_SETTINGS_SHEET
		Map<String, Map<PARAM_CONFIG, String>> reportParams = new LinkedHashMap<String, Map<PARAM_CONFIG, String>>(2);
		List<XSSFTable> tables = ((XSSFSheetImpl) settingsSheet).getTables();
		if (CollectionUtils.isEmpty(tables)) {
			return reportParams;
		}
		
		if (combine) {
			for (XSSFTable table : tables) {
				if (PARAM_TABLE.equals(table.getName())) {
					CellReference startCell = table.getStartCellReference();
					CellReference endCell = table.getEndCellReference();
					for (int i = startCell.getRow() + 1; i <= endCell.getRow(); i++) {
						
						String paramName = settingsSheet.getRow(i).getCell(startCell.getCol() + 1).getStringCellValue();
						if (StringUtils.hasText(paramName)) {
							if (contextParams.containsKey(paramName)) {
								settingsSheet.getRow(i).getCell(startCell.getCol() + 2).setCellValue(contextParams.get(paramName));
							}
						}
					}
				}
			}
			settingsSheet.getWorkbook().getCreationHelper().createFormulaEvaluator().evaluateAll();
		}
		
		for (XSSFTable table : tables) {
			if (PARAM_TABLE.equals(table.getName())) {
				CellReference startCell = table.getStartCellReference();
				CellReference endCell = table.getEndCellReference();
				for (int i = startCell.getRow() + 1; i <= endCell.getRow(); i++) {
					// Get parameter configuration by row
					Map<PARAM_CONFIG, String> paramConfig = new HashMap<PARAM_CONFIG, String>(4);
					
					for (PARAM_CONFIG pc : PARAM_CONFIG.values()) {
						int index = pc.ordinal() + startCell.getCol() + 1;
						// Combine URL prefix
						String prefix = "";
						if (pc.equals(PARAM_CONFIG.FETCH_URL)) {
							prefix = contextParams.get(URL);
						}
						if (index <= endCell.getCol()) {
							Cell c = settingsSheet.getRow(i).getCell(index);
							paramConfig.put(pc, prefix + (c.getCellType() == Cell.CELL_TYPE_NUMERIC ? String.valueOf(c.getNumericCellValue()) : c.getStringCellValue()));
						}
					}
					String paramName = settingsSheet.getRow(i).getCell(startCell.getCol()).getStringCellValue();
					// Fix parameter label text is empty text. Add by MENGRAN at 2012-10-23
					if (!StringUtils.hasText(paramName)) {
						StringBuilder sb = new StringBuilder();
						for (int spaceI = 0; spaceI <= i; spaceI++) {
							sb.append(" ");
						}
						paramName = sb.toString();
					}
					reportParams.put(paramName, paramConfig);
				}
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
				LOG.info("Use data retriever: " + entry.getKey());
				long startTime = System.currentTimeMillis();
				LOG.debug("Start processInput " + startTime);
				entry.getValue().retrieveData(inputSheet, reportParams);
				LOG.debug("End processInput " + (System.currentTimeMillis() - startTime));
				return;
			}
		}
		
		LOG.warn("Do nothing because unknown data type: " + reportParams.get(DATA_TYPE));
		return; 
		
	}
	
	protected void processSettings(Worksheet settingsSheet, Map<String, String> reportParams, boolean combine) {
		
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
			if ("reload".equals(row.getCell(0).getStringCellValue())) {
				reload = row.getCell(1).getBooleanCellValue();
			}
			if ("dataType".equals(row.getCell(0).getStringCellValue())) {
				type = row.getCell(1).getStringCellValue();
			}
		}
		reportParams.put(RELOAD, reload.toString());
		reportParams.put(DATA_TYPE, type);
		
		// Report parameter's value is high-priority
		if (!reportParams.containsKey(USERNAME) || reportParams.get(USERNAME) == null) {
			reportParams.put(USERNAME, username);
		}
		if (!reportParams.containsKey(PASSWORD) || reportParams.get(PASSWORD) == null) {
			reportParams.put(PASSWORD, password);
		}
		if (!reportParams.containsKey(URL) || reportParams.get(URL) == null) {
			reportParams.put(URL, url);
		}
		
		if (url != null && username != null) {
			LOG.info("Retrieve datesource settings: " + url + " " + username);
		}
		
		Map<String, Map<PARAM_CONFIG, String>> parameters = retrieveReportParamsFromSettingsSheet(settingsSheet, reportParams, combine);
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
