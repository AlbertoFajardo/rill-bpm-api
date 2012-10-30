package nu.com.rill.analysis.report.excel.data;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.Properties;
import java.util.WeakHashMap;

import javax.sql.DataSource;

import nu.com.rill.analysis.report.REException;
import nu.com.rill.analysis.report.excel.DataRetriever;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.dbcp.BasicDataSourceFactory;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.codehaus.jackson.map.JsonMappingException;
import org.codehaus.jackson.type.TypeReference;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.util.Assert;
import org.zkoss.poi.ss.usermodel.Cell;
import org.zkoss.poi.ss.usermodel.Row;
import org.zkoss.zss.model.Worksheet;

public class JdbcDataRetriever implements DataRetriever {

	public final Log LOGGER = LogFactory.getLog(this.getClass());
	
	private Map<String, DataSource> map = new WeakHashMap<String, DataSource>();
	
	private Properties dbcpProperties;
	
	public final Properties getDbcpProperties() {
		return dbcpProperties;
	}

	public final void setDbcpProperties(Properties dbcpProperties) {
		this.dbcpProperties = dbcpProperties;
	}

	@Override
	public boolean supportType(DATA_TYPE dt) {
		
		return DATA_TYPE.jdbc.equals(dt);
	}
	
	protected synchronized DataSource retrieveDataSource(String url, String userName, String password) {
		
		Assert.notNull(url);
		Assert.notNull(userName);
		Assert.notNull(password);
		
		// FIXME: MENGRAN. Extension point here. Simply we can construct data source, 
		// or we should hold a key-value pair to quickly retrieve action.
		for (int i = 0; i < 3 ; i++) {
			String key = url + userName + password;
			DataSource dataSource = map.get(key);
			if (dataSource == null) {
				Properties useProperties = new Properties();
				useProperties.putAll(getDbcpProperties());
				useProperties.put("url", url);
				useProperties.put("username", userName);
				useProperties.put("password", password);
				try {
					dataSource = BasicDataSourceFactory.createDataSource(useProperties);
					map.put(key, dataSource);
					LOGGER.debug("Construct a data source(dbcp) and record it. " + url + " username " + userName);
				} catch (Exception e) {
					LOGGER.error("Error to construct dbcp datasource." + url + " " + "username " + userName, e);
					throw new REException("无法建立数据库连接。");
				}
			}
			
			dataSource = map.get(key);
			if (dataSource == null) {
				LOGGER.info("Can not retrieve datasource, we try again. " + i);
			} else {
				return dataSource;
			}
		}
		
		throw new REException("无法连接数据库（重试3次）。");
	}

	@SuppressWarnings("unchecked")
	@Override
	public void retrieveData(Worksheet dataSheet, Map<String, String> reportParams) {
		
		String url = reportParams.get(ReportEngine.URL);
		String userName = reportParams.get(ReportEngine.USERNAME);
		String password = reportParams.get(ReportEngine.PASSWORD);
		DataSource dataSource = retrieveDataSource(url, userName, password);
		JdbcTemplate template = new JdbcTemplate(dataSource);
		
		// Start execute SQL to retrieve result.
		Row row = dataSheet.getRow(dataSheet.getFirstRowNum());
//		String url = row.getCell(row.getFirstCellNum()).getStringCellValue();
		String result = ReportEngine.fetchUrl(reportParams.get(ReportEngine.URL) + url, reportParams);
		
		List<List<String>> data = null;
		try {
			Map<String, Object> jsonResult = new LinkedHashMap<String, Object>();
			try {
				jsonResult = ReportEngine.mapper.readValue(result, new TypeReference<Map<String, Object>>() {
				});
			} catch (JsonMappingException e) {
				// Ignore 
				LOGGER.debug("Fail to read value as Map<String, Object>, ignore it." + result);
			}
			if (jsonResult.containsKey("_RE_DATA_JSON_RESULT")) {
				data = (List<List<String>>) jsonResult.get("_RE_DATA_JSON_RESULT");
			}
			
			if (data == null) {
				data = new ArrayList<List<String>>();
				data.addAll(ReportEngine.mapper.readValue(result, List.class));
			}
			
			int i = row.getRowNum() + 1;
			for (List<String> element : data) {
				// Handle row one by one.
				Row currentRow = null;
				if (dataSheet.getRow(i) == null) {
					currentRow = ReportEngine.copyRow(dataSheet, i - 1, i);
				} else {
					currentRow = dataSheet.getRow(i);
				}
				for (int j = 0; j < element.size(); j++) {
					Cell currentCell = null;
					int cellType = Cell.CELL_TYPE_STRING;
					if (currentRow.getCell(j) == null) {
						currentCell = currentRow.createCell(j);
						currentCell.setCellStyle(currentRow.getCell(j - 1).getCellStyle());
						currentCell.setCellType(currentRow.getCell(j - 1).getCellType());
						cellType = currentRow.getCell(j - 1).getCellType();
					} else {
						currentCell = currentRow.getCell(j);
						cellType = currentRow.getCell(j).getCellType();
					}
					if (Cell.CELL_TYPE_NUMERIC == cellType) {
						currentCell.setCellValue(new Double(element.get(j)));
					}
					if (Cell.CELL_TYPE_STRING == cellType) {
						currentCell.setCellValue(element.get(j));
					}
				}
				i++;
			}
		} catch (Exception e) {
			LOGGER.error("Error when try to parse to JSON " + result, e);
			throw new REException("仅允许响应application/json数据.", e);
		}
		
		return;
	}

}
