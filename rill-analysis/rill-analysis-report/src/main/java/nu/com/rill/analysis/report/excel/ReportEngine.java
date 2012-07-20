package nu.com.rill.analysis.report.excel;

import java.io.ByteArrayInputStream;
import java.io.InputStream;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import org.apache.commons.io.output.ByteArrayOutputStream;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.map.ObjectMapper;
import org.codehaus.jackson.node.ObjectNode;
import org.rill.bpm.api.WorkflowOperations;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.poi.ss.usermodel.Workbook;
import org.zkoss.poi.xssf.usermodel.XSSFWorkbook;
import org.zkoss.zss.model.Book;
import org.zkoss.zss.model.Worksheet;
import org.zkoss.zss.model.impl.ExcelImporter;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.representation.Form;

/**
 * Excel report engine. 
 * @author mengran
 *
 */
public class ReportEngine {

	public static final String _SETTINGS_SHEET = "_settings";
	public static final String _INPUT_SHEET = "_input";
	public static final char FULL_LENGTH_QUESTING = '\uFF1F';
	
	public static final String REPORT_PARAMETERS = "REPORT_PARAMETERS";
	public static final String REPORT_SESSION = "REPORT_SESSION";
	public static final String REPORT_QUERYNAME = "REPORT_QUERYNAME";
	public static final String REPORT_DISCOVERY = "REPORT_DISCOVERY";
	public static final String REPORT_CUBE = "REPORT_CUBE";
	
	public static final String JSESSIONID = "JSESSIONID";
	public static final String RELOAD = "RELOAD";
	public static final String USERNAME = "USERNAME";
	public static final String PASSWORD = "PASSWORD";
	public static final String URL = "URL";
	
	public final Log LOG = LogFactory.getLog(this.getClass());
	
	static Client client = Client.create();
	static ObjectMapper mapper = new ObjectMapper();
	
	public static final ReportEngine INSTANCE = new ReportEngine();
	
	private ReportEngine() {
		// Singleton
	}
	
	public Workbook generateReport(InputStream is, String bookName, Map<String, String> reportParams) {
		
		Assert.notNull(is);
		Assert.notNull(bookName);
		
		Book book = new ExcelImporter().imports(is, bookName);
		
		try {
			// 1. Validate
			validateReportTemplate(book);
			
			// 2. Handle #_SETTINGS_SHEET
			processSettings(book.getWorksheet(_SETTINGS_SHEET), reportParams);
			
			// 3. Handle #_INPUT_SHEET sheet
			processInput(book.getWorksheet(_INPUT_SHEET), reportParams);
			
			// 4. Formula evaluate
//			bookAfterProcess.setForceFormulaRecalculation(true);
			book.getCreationHelper().createFormulaEvaluator().evaluateAll();
			
			// 5. Clone book
			ByteArrayOutputStream baos = new ByteArrayOutputStream();
			book.write(baos);
			Workbook bookAfterProcess = new ExcelImporter().imports(new ByteArrayInputStream(baos.toByteArray()), bookName);
			
			return bookAfterProcess;
			
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
	}
	
	protected void processInput(Worksheet inputSheet, Map<String, String> reportParams) throws Exception {
		
		// 1. Check reload
		if (!new Boolean(true).toString().equals(reportParams.get(RELOAD))) {
			LOG.info("Disable reload feature: " + reportParams);
			return; 
		}
		
		// 2. Reload data
		@SuppressWarnings("unchecked")
		List<Map<String, Object>> saikuConnections = mapper.readValue(reportParams.get(REPORT_DISCOVERY), List.class);
		
		for (Row row : inputSheet) {
			if (row.getCell(0).getCellType() == Cell.CELL_TYPE_STRING && row.getCell(0).getStringCellValue().startsWith("SELECT")) {
				LOG.debug("Replace data begin with next row using " + row.getCell(0).getStringCellValue());
				// Determine if need delete query or not
				if (reportParams.containsKey(REPORT_CUBE) && !row.getCell(0).getStringCellValue().contains("[" + reportParams.containsKey(REPORT_CUBE) + "]")) {
					Builder deleteBuilder = requestBuilder(reportParams.get(URL) + "saiku/" + reportParams.get(USERNAME) + "/query/" + reportParams.get(REPORT_QUERYNAME), reportParams);
					deleteBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
							.accept(MediaType.APPLICATION_JSON_TYPE)
							.delete();
					reportParams.remove(REPORT_CUBE);
				}
				
				if (!reportParams.containsKey(REPORT_CUBE)) {
					String cubeName = row.getCell(0).getStringCellValue().substring(row.getCell(0).getStringCellValue().indexOf("FROM "));
					cubeName = cubeName.substring(6, cubeName.indexOf("]"));
					for (Map<String, Object> sc : saikuConnections) {
						@SuppressWarnings("unchecked")
						List<Map<String, Object>> catalogs = (List<Map<String, Object>>) sc.get("catalogs");
						for (Map<String, Object> ss : catalogs) {
							@SuppressWarnings("unchecked")
							List<Map<String, Object>> schemas = (List<Map<String, Object>>) ss.get("schemas");
							for (Map<String, Object> scube : schemas) {
								@SuppressWarnings("unchecked")
								List<Map<String, String>> cubes = (List<Map<String, String>>) scube.get("cubes");
								for (Map<String, String> cs : cubes) {
									if (cs.get("name").equals(cubeName)) {
										Form form = new Form();
										form.add("connection", cs.get("connectionName"));
										form.add("catalog", cs.get("catalogName"));
										form.add("schema", cs.get("schemaName"));
										form.add("cube", cs.get("name"));
										// New query
										newQuery(reportParams);
										Builder newQueryBuilder = requestBuilder(reportParams.get(URL) + "saiku/" + reportParams.get(USERNAME) + "/query/" + reportParams.get(REPORT_QUERYNAME), reportParams);
										newQueryBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
											.accept(MediaType.APPLICATION_JSON_TYPE)
											.post(form);
									}
								}
							}
						}
						
					}
					
				}
				
				// Retrieve data
				Builder mdxBuilder = requestBuilder(reportParams.get(URL) + "saiku/" + reportParams.get(USERNAME) + "/query/" + reportParams.get(REPORT_QUERYNAME) + "/mdx", reportParams);
				mdxBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.get(String.class);
				
				// Change report parameter
				String mdx = row.getCell(0).getStringCellValue();
				if (reportParams.containsKey(REPORT_PARAMETERS)) {
					@SuppressWarnings("unchecked")
					Map<String, String> parameters = WorkflowOperations.XStreamSerializeHelper.deserializeObject(reportParams.get(REPORT_PARAMETERS), REPORT_PARAMETERS, Map.class);
					for (Entry<String, String> entry : parameters.entrySet()) {
						LOG.debug("Replace " + entry.getKey() + " report parameters using " + entry.getValue());
						mdx = StringUtils.replace(mdx, entry.getKey(), entry.getValue());
					}
				}
				LOG.debug("Execute mdx " + mdx);
				
				Form form = new Form();
				form.add("mdx", mdx);
				Builder mdxActionBuilder = requestBuilder(reportParams.get(URL) + "saiku/" + reportParams.get(USERNAME) + "/query/" + reportParams.get(REPORT_QUERYNAME) + "/result/flattened", reportParams);
				String mdxResult = mdxActionBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
					.accept(MediaType.APPLICATION_JSON_TYPE)
					.post(String.class, form);
				
				// Replace result data in sheet
				JsonParser parser = mapper.getJsonFactory().createJsonParser(mdxResult);
				parser.nextToken();
				while (parser.nextToken() != JsonToken.END_OBJECT) {
					String fieldname = parser.getCurrentName();
					parser.nextToken();
					if (fieldname.equals("cellset")) {
						JsonNode cellset = parser.readValueAsTree();
						for (int i = 0; i < cellset.size(); i++) {
							JsonNode child = cellset.get(i);
							Row childRow = inputSheet.getRow(row.getRowNum() + i + 1);
							for (int j = 0; j < child.size(); j++) {
								JsonNode on = child.get(j);
								Assert.isTrue(on instanceof ObjectNode, on + " should be ObjectNode but " + on.getClass().getName());
								if (childRow.getCell(j).getCellType() == Cell.CELL_TYPE_NUMERIC && on.findValue("type").getValueAsText().equals("DATA_CELL")) {
									childRow.getCell(j).setCellValue(new Double(on.findValue("raw").getValueAsText()));
								} else if (childRow.getCell(j).getCellType() == Cell.CELL_TYPE_BLANK) {
									// Not change blank value
								} else {
									childRow.getCell(j).setCellValue(on.findValue("value").getValueAsText());
								}
							}
						}
 					}
				}
				parser.close();
			}
		}
		
	}
	
	private Builder requestBuilder(String url, Map<String, String> reportParams) {
		
		WebResource r = client.resource(url);
		NewCookie nc = WorkflowOperations.XStreamSerializeHelper.deserializeObject(reportParams.get(JSESSIONID), JSESSIONID, NewCookie.class);
		Builder b = r.getRequestBuilder();
		b.cookie(nc);
		
		return b;
	}
	
	private String newQuery(Map<String, String> reportParams) {
		
		String queryName = UUID.randomUUID().toString();
		reportParams.put(REPORT_QUERYNAME, queryName);
		
		return queryName;
	}
	
	protected void connectToServer(String url, String username, String password, Map<String, String> reportParams) throws Exception {
		
		// Login
		Form form = new Form();
		form.add("username", username);
		form.add("password", password);
		WebResource r = client.resource(url + "saiku/session");
		ClientResponse response = r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, form);
		for (NewCookie cookie : response.getCookies()) {
			if (JSESSIONID.equals(cookie.getName())) {
				reportParams.put(JSESSIONID, WorkflowOperations.XStreamSerializeHelper.serializeXml(JSESSIONID, cookie));
				LOG.debug("Retrieve session id " + cookie.getValue() + " for user " + username + " request.");
			}
		}
		WebResource getR = client.resource(url + "saiku/session");
		NewCookie nc = WorkflowOperations.XStreamSerializeHelper.deserializeObject(reportParams.get(JSESSIONID), JSESSIONID, NewCookie.class);
		Builder b = getR.getRequestBuilder();
		b.cookie(nc);
		String session = b.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(String.class);
		reportParams.put(REPORT_SESSION, session);
		
		// Retrieve all cube
		Builder discoverB = requestBuilder(url + "saiku/" + username + "/discover/", reportParams);
		String discover = discoverB.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get(String.class);
		reportParams.put(REPORT_DISCOVERY, discover);
		
		newQuery(reportParams);
		
	}
	
	protected void processSettings(Worksheet settingsSheet, Map<String, String> reportParams) {
		
		String url = null, username = null, password = null;
		Boolean reload = new Boolean(true);
		for (Row row : settingsSheet) {
			if ("url".equals(row.getCell(0).getStringCellValue())) {
				url = row.getCell(1).getStringCellValue();
			}
			// FIXME: User name and password need retrieve from report parameter???
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
		}
		reportParams.put(RELOAD, reload.toString());
		reportParams.put(URL, url);
		reportParams.put(USERNAME, username);
		reportParams.put(PASSWORD, PASSWORD);
		
		// Connect to server
		try {
			connectToServer(url, username, password, reportParams);
		} catch (Exception e) {
			LOG.error("Can not connect to data server " + reportParams, e);
			throw new RuntimeException(e);
		}
		
		Map<String, String> parameters = new HashMap<String, String>();
		for (Row row : settingsSheet) {
			if (row.getLastCellNum() > 4 && !"".equals(row.getCell(3).getStringCellValue()) 
					&& ("?".equals(row.getCell(4).getStringCellValue()) 
							|| String.valueOf(FULL_LENGTH_QUESTING).equals(row.getCell(4).getStringCellValue()))) {
				if (!reportParams.containsKey(row.getCell(3).getStringCellValue())) {
					throw new IllegalArgumentException(row.getCell(3).getStringCellValue() + " is a report parameter, but pass-in actual value in report parameters: " + reportParams);
				}
				parameters.put(row.getCell(3).getStringCellValue(), reportParams.get(row.getCell(3).getStringCellValue()));
			}
		}
		if (!CollectionUtils.isEmpty(parameters)) {
			reportParams.put(REPORT_PARAMETERS, WorkflowOperations.XStreamSerializeHelper.serializeXml(REPORT_PARAMETERS, parameters));
			LOG.info("Have added " + REPORT_PARAMETERS + " to report params: " + reportParams);
		}
		
	}
	
	protected void validateReportTemplate(Book book) {
		
		Worksheet sheet = book.getWorksheet(_SETTINGS_SHEET);
		if (sheet == null) {
			throw new IllegalArgumentException(book.getBookName() + " not contains sheet: " + _SETTINGS_SHEET);
		}
		sheet = book.getWorksheet(_INPUT_SHEET);
		if (sheet == null) {
			throw new IllegalArgumentException(book.getBookName() + " not contains sheet: " + _INPUT_SHEET);
		}
		
		if (!(book instanceof XSSFWorkbook)) {
			throw new UnsupportedOperationException("Only support Excel 2007~. " + book.getBookName());
		}
		
	}
}
