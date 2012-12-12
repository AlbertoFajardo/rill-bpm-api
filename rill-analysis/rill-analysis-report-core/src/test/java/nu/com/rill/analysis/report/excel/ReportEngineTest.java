package nu.com.rill.analysis.report.excel;

import java.io.IOException;
import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;

import org.apache.commons.lang.time.DateUtils;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;
import org.springframework.util.StringUtils;

public class ReportEngineTest {
	
	@Test
	public void retrieveReportParamsLuopan() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		
		String SELECTED_DATE = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -1));
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan.xlsx");
		try {
			Map<String, String> contextParams = new HashMap<String, String>();
			contextParams.put(ReportEngine.URL, "DUMMY");
			Map<String, Map<PARAM_CONFIG, String>> list = re.retrieveReportParams(cpr.getInputStream(), "luopan.xlsx", contextParams);
			Assert.assertTrue(list.size() == 7);
			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.NAME).equals("lineId"));
			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.RENDER_TYPE).equals("select"));
			Assert.assertTrue(StringUtils.hasText(list.get("商业产品线").get(PARAM_CONFIG.FETCH_URL)));
			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.DEPENDENCIES).equals("lineId"));
			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.FETCH_URL).equals("DUMMYexample/ind"));
			Assert.assertTrue(list.get("选择日期").get(PARAM_CONFIG.VALUE).equals(SELECTED_DATE));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
	@Test
	public void retrieveReportParamsLuopanNoContext() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/luopan.xlsx");
		try {
			Map<String, Map<PARAM_CONFIG, String>> list = re.retrieveReportParams(cpr.getInputStream(), "luopan.xlsx");
			Assert.assertTrue(list.size() == 7);
			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.NAME).equals("lineId"));
			Assert.assertTrue(list.get("商业产品线").get(PARAM_CONFIG.RENDER_TYPE).equals("select"));
			Assert.assertTrue(StringUtils.hasText(list.get("商业产品线").get(PARAM_CONFIG.FETCH_URL)));
			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.DEPENDENCIES).equals("lineId"));
			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.FETCH_URL).endsWith("example/ind"));
			Assert.assertTrue(list.get("分析指标").get(PARAM_CONFIG.FETCH_URL).startsWith("http://"));
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
	}
	
}
