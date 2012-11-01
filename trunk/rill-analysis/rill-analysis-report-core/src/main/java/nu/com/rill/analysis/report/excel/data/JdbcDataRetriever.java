package nu.com.rill.analysis.report.excel.data;

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
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.config.PropertyPlaceholderConfigurer;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.support.rowset.SqlRowSet;
import org.springframework.util.Assert;
import org.springframework.util.PropertyPlaceholderHelper;
import org.springframework.util.PropertyPlaceholderHelper.PlaceholderResolver;
import org.springframework.util.StringValueResolver;
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
	
	// ------------------------------------------------------------- Start
	
	// --------- Copy from Spring framework[PropertyPlaceholderConfigurer]
	
	/** Default placeholder prefix: {@value} */
	public static final String DEFAULT_PLACEHOLDER_PREFIX = "${";

	/** Default placeholder suffix: {@value} */
	public static final String DEFAULT_PLACEHOLDER_SUFFIX = "}";

	/** Default value separator: {@value} */
	public static final String DEFAULT_VALUE_SEPARATOR = ":";


	/** Defaults to {@value #DEFAULT_PLACEHOLDER_PREFIX} */
	protected String placeholderPrefix = DEFAULT_PLACEHOLDER_PREFIX;

	/** Defaults to {@value #DEFAULT_PLACEHOLDER_SUFFIX} */
	protected String placeholderSuffix = DEFAULT_PLACEHOLDER_SUFFIX;

	/** Defaults to {@value #DEFAULT_VALUE_SEPARATOR} */
	protected String valueSeparator = DEFAULT_VALUE_SEPARATOR;
	
	protected boolean ignoreUnresolvablePlaceholders = false;
	
	private class PlaceholderResolvingStringValueResolver implements StringValueResolver {

		private final PropertyPlaceholderHelper helper;

		private final PlaceholderResolver resolver;

		public PlaceholderResolvingStringValueResolver(Properties props) {
			this.helper = new PropertyPlaceholderHelper(
					placeholderPrefix, placeholderSuffix, valueSeparator, ignoreUnresolvablePlaceholders);
			this.resolver = new PropertyPlaceholderConfigurerResolver(props);
		}

		public String resolveStringValue(String strVal) throws BeansException {
			String value = this.helper.replacePlaceholders(strVal, this.resolver);
			return (value.equals(null) ? null : value);
		}
	}
	
	private class PropertyPlaceholderConfigurerResolver implements PlaceholderResolver {

		private final Properties props;

		private PropertyPlaceholderConfigurerResolver(Properties props) {
			this.props = props;
		}

		public String resolvePlaceholder(String placeholderName) {
			return JdbcDataRetriever.this.resolvePlaceholder(placeholderName, props, PropertyPlaceholderConfigurer.SYSTEM_PROPERTIES_MODE_FALLBACK);
		}
	}
	
	/**
	 * Resolve the given placeholder using the given properties, performing
	 * a system properties check according to the given mode.
	 * <p>The default implementation delegates to <code>resolvePlaceholder
	 * (placeholder, props)</code> before/after the system properties check.
	 * <p>Subclasses can override this for custom resolution strategies,
	 * including customized points for the system properties check.
	 * @param placeholder the placeholder to resolve
	 * @param props the merged properties of this configurer
	 * @param systemPropertiesMode the system properties mode,
	 * according to the constants in this class
	 * @return the resolved value, of null if none
	 * @see #setSystemPropertiesMode
	 * @see System#getProperty
	 * @see #resolvePlaceholder(String, java.util.Properties)
	 */
	protected String resolvePlaceholder(String placeholder, Properties props, int systemPropertiesMode) {
		String propVal = props.getProperty(placeholder);
		return propVal;
	}
	
	// ------------------------------------------------------------- End

	@Override
	public void retrieveData(Worksheet dataSheet, Map<String, String> reportParams) {
		
		String url = reportParams.get(ReportEngine.URL);
		String userName = reportParams.get(ReportEngine.USERNAME);
		String password = reportParams.get(ReportEngine.PASSWORD);
		DataSource dataSource = retrieveDataSource(url, userName, password);
		JdbcTemplate template = new JdbcTemplate(dataSource);
		
		// Start execute SQL to retrieve result.
		Row row = dataSheet.getRow(dataSheet.getFirstRowNum());
		String sql = row.getCell(row.getFirstCellNum()).getStringCellValue();
		LOGGER.debug("SQL(retrieve from tempalte) = " + sql);
		// FIXME: MENGRAN. Call jdbcTemplate is safe?
		Properties props = new Properties();
		props.putAll(reportParams);
		StringValueResolver valueResolver = new PlaceholderResolvingStringValueResolver(props);
		String afterResolvedPlaceHolder = valueResolver.resolveStringValue(sql);
		LOGGER.debug("SQL(after resolved place-holder) = " + afterResolvedPlaceHolder);
		
		try {
			SqlRowSet rowSet = template.queryForRowSet(afterResolvedPlaceHolder);
			rowSet.beforeFirst();
			int i = row.getRowNum() + 1;
			Row titleRow = dataSheet.getRow(i);
			if (titleRow == null) {
				titleRow = dataSheet.createRow(i);
			} else {
				titleRow = dataSheet.getRow(i);
			}
			for (int k = 0; k < rowSet.getMetaData().getColumnCount(); k++) {
				int cellType = Cell.CELL_TYPE_STRING;
				Cell currentCell = null;
				if (titleRow.getCell(k) == null) {
					currentCell = titleRow.createCell(k);
					currentCell.setCellType(cellType);
				} else {
					currentCell = titleRow.getCell(k);
					cellType = titleRow.getCell(k).getCellType();
				}
				currentCell.setCellValue(rowSet.getMetaData().getColumnLabel(k + 1));
			}
			i++;
			while (rowSet.next()) {
				i++;
				// Handle row one by one.
				Row currentRow = null;
				if (dataSheet.getRow(i) == null) {
					currentRow = ReportEngine.copyRow(dataSheet, i - 1, i);
				} else {
					currentRow = dataSheet.getRow(i);
				}
				for (int j = 0; j < rowSet.getMetaData().getColumnCount(); j++) {
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
						currentCell.setCellValue(rowSet.getDouble(j + 1));
					}
					if (Cell.CELL_TYPE_STRING == cellType) {
						currentCell.setCellValue(rowSet.getString(j + 1));
					}
				}
				i++;
			}
			
		} catch (DataAccessException e) {
			LOGGER.error("Error when try to execute SQL " + afterResolvedPlaceHolder, e);
			throw new REException("无法执行SQL获取数据.", e);
		} catch (Exception e) {
			LOGGER.error("Error when try to extract SQL resultSet " + afterResolvedPlaceHolder, e);
			throw new REException("无法解析SQL返回结果.", e);
		}
		
		return;
	}

}
