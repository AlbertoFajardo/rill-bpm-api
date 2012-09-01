package nu.com.rill.analysis.report.bo;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.LinkedHashMap;
import java.util.Map;
import java.util.Map.Entry;

import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

import org.rill.bpm.api.WorkflowOperations;
import org.springframework.util.CollectionUtils;
import org.springframework.util.StringUtils;

public class Report {

	private Integer id;
	
	private String name;
	
	private Map<String, Map<PARAM_CONFIG, String>> params;
	private String paramsXStrem;
	
	private String cronExpression;
	
	private byte[] reportContent;
	
	private Date addDate;

	public final Integer getId() {
		return id;
	}

	public final void setId(Integer id) {
		this.id = id;
	}

	public final String getName() {
		return name;
	}

	public final void setName(String name) {
		this.name = name;
	}

	public final Map<String, Map<PARAM_CONFIG, String>> getParams() {
		
		return params == null ? deserializeParam(this.paramsXStrem) : params;
	}

	public final String getCronExpression() {
		return cronExpression;
	}

	public final void setCronExpression(String cronExpression) {
		this.cronExpression = cronExpression;
	}

	public final String getParamsXStrem() {
		
		return paramsXStrem;
	}

	/**
	 * For DAO. Not for application
	 * @param paramsXStrem
	 */
	public final void setParamsXStrem(String paramsXStrem) {
		this.paramsXStrem = paramsXStrem;
	}

	public final byte[] getReportContent() {
		return reportContent;
	}

	public final void setReportContent(byte[] reportContent) {
		this.reportContent = reportContent;
	}

	public final Date getAddDate() {
		return addDate;
	}

	public final void setAddDate(Date addDate) {
		this.addDate = addDate;
	}
	
	public final String getAddDateFormatString() {
		
		return addDate == null ? "" : new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").format(addDate);
	}
	
	public static String serializeParams(Map<String, Map<PARAM_CONFIG, String>> params) {
		
		if (CollectionUtils.isEmpty(params)) {
			return null;
		}
		
		String reportParams = "";
		for (Entry<String, Map<PARAM_CONFIG, String>> entry : params.entrySet()) {
			reportParams = reportParams + "|" + entry.getKey() + ":" + WorkflowOperations.XStreamSerializeHelper.serializeXml("PARAM_CONFIG", entry.getValue());
		}
		if (reportParams.length() > 0) {
			reportParams = reportParams.substring(1);
		}
		
		return reportParams;
	}
	
	@SuppressWarnings("unchecked")
	public static Map<String, Map<PARAM_CONFIG, String>> deserializeParam(String paramsXStream) {
		
		if (!StringUtils.hasText(paramsXStream)) {
			return null;
		}
		
		Map<String, Map<PARAM_CONFIG, String>> params = new LinkedHashMap<String, Map<PARAM_CONFIG,String>>();
		for (String param : paramsXStream.split("\\|")) {
			params.put(param.split(":")[0], WorkflowOperations.XStreamSerializeHelper.deserializeObject(param.split(":")[1].replaceAll("\n", ""), "PARAM_CONFIG", LinkedHashMap.class));
		}
		
		return params;
	}
	
}
