package nu.com.rill.analysis.report.excel.data;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.List;
import java.util.Locale;
import java.util.Map;
import java.util.Map.Entry;
import java.util.UUID;

import javax.ws.rs.core.MediaType;
import javax.ws.rs.core.NewCookie;

import mondrian.util.Format;
import nu.com.rill.analysis.report.excel.DataRetriever;
import nu.com.rill.analysis.report.excel.ReportEngine;
import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.JsonNode;
import org.codehaus.jackson.JsonParser;
import org.codehaus.jackson.JsonToken;
import org.codehaus.jackson.node.ObjectNode;
import org.rill.bpm.api.WorkflowOperations;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.zss.model.Worksheet;

import com.sun.jersey.api.client.Client;
import com.sun.jersey.api.client.ClientResponse;
import com.sun.jersey.api.client.WebResource;
import com.sun.jersey.api.client.WebResource.Builder;
import com.sun.jersey.api.representation.Form;

public class MdxDataRetriever implements DataRetriever {

	public final Log LOG = LogFactory.getLog(this.getClass());
	
	@Override
	public boolean supportType(DATA_TYPE dt) {
		
		return DATA_TYPE.mdx.equals(dt);
	}
	
	static Client client = Client.create();
	
	private Builder requestBuilder(String url, Map<String, String> reportParams) {
		
		WebResource r = client.resource(url);
		NewCookie nc = WorkflowOperations.XStreamSerializeHelper.deserializeObject(reportParams.get(ReportEngine.JSESSIONID), ReportEngine.JSESSIONID, NewCookie.class);
		Builder b = r.getRequestBuilder();
		b.cookie(nc);
		
		return b;
	}
	
	private String newQuery(Map<String, String> reportParams) {
		
		String queryName = UUID.randomUUID().toString();
		reportParams.put(ReportEngine.REPORT_QUERYNAME, queryName);
		
		return queryName;
	}
	
	private void connectToServer(Map<String, String> reportParams) throws Exception {
		
		String url = reportParams.get(ReportEngine.URL);
		String username = reportParams.get(ReportEngine.USERNAME);
		String password = reportParams.get(ReportEngine.PASSWORD);
		// Login
		Form form = new Form();
		form.add("username", username);
		form.add("password", password);
		WebResource r = client.resource(url + "saiku/session");
		ClientResponse response = r.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.post(ClientResponse.class, form);
		for (NewCookie cookie : response.getCookies()) {
			if (ReportEngine.JSESSIONID.equals(cookie.getName())) {
				reportParams.put(ReportEngine.JSESSIONID, WorkflowOperations.XStreamSerializeHelper.serializeXml(ReportEngine.JSESSIONID, cookie));
				LOG.debug("Retrieve session id " + cookie.getValue() + " for user " + username + " request.");
			}
		}
		WebResource getR = client.resource(url + "saiku/session");
		NewCookie nc = WorkflowOperations.XStreamSerializeHelper.deserializeObject(reportParams.get(ReportEngine.JSESSIONID), ReportEngine.JSESSIONID, NewCookie.class);
		Builder b = getR.getRequestBuilder();
		b.cookie(nc);
		String session = b.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
				.accept(MediaType.APPLICATION_JSON_TYPE)
				.get(String.class);
		reportParams.put(ReportEngine.REPORT_SESSION, session);
		
		// Retrieve all cube
		Builder discoverB = requestBuilder(url + "saiku/" + username + "/discover/", reportParams);
		String discover = discoverB.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
			.accept(MediaType.APPLICATION_JSON_TYPE)
			.get(String.class);
		reportParams.put(ReportEngine.REPORT_DISCOVERY, discover);
		
		newQuery(reportParams);
		
	}

	@Override
	public void retrieveData(Worksheet dataSheet, Map<String, String> reportParams) {
		
		final Locale locale = Locale.getDefault();
        final Format format = new Format(ReportEngine.REPORT_SCHEDULE_FORMAT, locale);
        String currDateStr = format.format(new Date());
		
	    // Connect to server
		try {
			connectToServer(reportParams);
		} catch (Exception e) {
			LOG.error("Can not connect to data server " + reportParams, e);
			throw new RuntimeException(e);
		}
     	
		try {
			// 2. Reload data
			@SuppressWarnings("unchecked")
			List<Map<String, Object>> saikuConnections = ReportEngine.mapper.readValue(reportParams.get(ReportEngine.REPORT_DISCOVERY), List.class);
			
			for (Row row : dataSheet) {
				if (row.getCell(0) != null && row.getCell(0).getCellType() == Cell.CELL_TYPE_STRING && row.getCell(0).getStringCellValue().startsWith("SELECT")) {
					LOG.debug("Replace data begin with next row using " + row.getCell(0).getStringCellValue());
					// Determine if need delete query or not
					if (reportParams.containsKey(ReportEngine.REPORT_CUBE) && !row.getCell(0).getStringCellValue().contains("[" + reportParams.containsKey(ReportEngine.REPORT_CUBE) + "]")) {
						Builder deleteBuilder = requestBuilder(reportParams.get(ReportEngine.URL) + "saiku/" + reportParams.get(ReportEngine.USERNAME) + "/query/" + reportParams.get(ReportEngine.REPORT_QUERYNAME), reportParams);
						deleteBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
								.accept(MediaType.APPLICATION_JSON_TYPE)
								.delete();
						reportParams.remove(ReportEngine.REPORT_CUBE);
					}
					
					if (!reportParams.containsKey(ReportEngine.REPORT_CUBE)) {
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
											Builder newQueryBuilder = requestBuilder(reportParams.get(ReportEngine.URL) + "saiku/" + reportParams.get(ReportEngine.USERNAME) + "/query/" + reportParams.get(ReportEngine.REPORT_QUERYNAME), reportParams);
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
					Builder mdxBuilder = requestBuilder(reportParams.get(ReportEngine.URL) + "saiku/" + reportParams.get(ReportEngine.USERNAME) + "/query/" + reportParams.get(ReportEngine.REPORT_QUERYNAME) + "/mdx", reportParams);
					mdxBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
						.accept(MediaType.APPLICATION_JSON_TYPE)
						.get(String.class);
					
					// Change report parameter
					String mdx = row.getCell(0).getStringCellValue();
					if (reportParams.containsKey(ReportEngine.REPORT_PARAMETERS)) {
						@SuppressWarnings("unchecked")
						Map<String, Map<PARAM_CONFIG, String>> parameters = WorkflowOperations.XStreamSerializeHelper.deserializeObject(reportParams.get(ReportEngine.REPORT_PARAMETERS), ReportEngine.REPORT_PARAMETERS, Map.class);
						for (Entry<String, Map<PARAM_CONFIG, String>> entry : parameters.entrySet()) {
							
							String replacedValue = null;
							// Value in report parameters is high-priority 
							replacedValue = reportParams.containsKey(entry.getKey()) ? reportParams.get(entry.getKey()) : replacedValue;
							// Schedule model, then use current date to change report parameters
							if (reportParams.get(ReportEngine.REPORT_SCHEDULE_MODE) != null) {
								// 1. Check parameter names is time dimension
								if (entry.getValue() != null && entry.getValue().get(PARAM_CONFIG.VALUE) != null 
										&& entry.getValue().get(PARAM_CONFIG.VALUE).split("\\.").length > 0 && currDateStr.startsWith(entry.getValue().get(PARAM_CONFIG.VALUE).split("\\.")[0])) {
									replacedValue = currDateStr.substring(0, entry.getValue().get(PARAM_CONFIG.VALUE).length());
								}
							} else {
								if ("calendar".equals(entry.getValue().get(PARAM_CONFIG.RENDER_TYPE))) {
									// 1. Check parameter names is time dimension
									if (entry.getValue() != null && entry.getValue().get(PARAM_CONFIG.VALUE) != null 
											&& entry.getValue().get(PARAM_CONFIG.VALUE).split("\\.").length > 0 && currDateStr.startsWith(entry.getValue().get(PARAM_CONFIG.VALUE).split("\\.")[0])) {
										String tempStr = format.format(new SimpleDateFormat(entry.getValue().get(PARAM_CONFIG.FORMAT)).parseObject(reportParams.get(entry.getKey())));
										replacedValue = tempStr.substring(0, entry.getValue().get(PARAM_CONFIG.VALUE).length());
									}
								}
							}
							
							LOG.debug("Replace " + entry.getValue() + " report parameters using " + replacedValue);
							mdx = StringUtils.replace(mdx, entry.getValue().get(PARAM_CONFIG.VALUE), replacedValue);
						}
					}
					LOG.debug("Execute mdx " + mdx);
					
					Form form = new Form();
					form.add("mdx", mdx);
					Builder mdxActionBuilder = requestBuilder(reportParams.get(ReportEngine.URL) + "saiku/" + reportParams.get(ReportEngine.USERNAME) + "/query/" + reportParams.get(ReportEngine.REPORT_QUERYNAME) + "/result/flattened", reportParams);
					String mdxResult = mdxActionBuilder.type(MediaType.APPLICATION_FORM_URLENCODED_TYPE)
						.accept(MediaType.APPLICATION_JSON_TYPE)
						.post(String.class, form);
					
					// Replace result data in sheet
					JsonParser parser = ReportEngine.mapper.getJsonFactory().createJsonParser(mdxResult);
					parser.nextToken();
					while (parser.nextToken() != JsonToken.END_OBJECT) {
						String fieldname = parser.getCurrentName();
						parser.nextToken();
						if (fieldname.equals("cellset")) {
							JsonNode cellset = parser.readValueAsTree();
							for (int i = 0; i < cellset.size(); i++) {
								JsonNode child = cellset.get(i);
								Row childRow = dataSheet.getRow(row.getRowNum() + i + 1);
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
		} catch (Exception e) {
			LOG.error("Error ocurred when try to reload data", e);
			throw new RuntimeException(e);
		}
		
	}

}
