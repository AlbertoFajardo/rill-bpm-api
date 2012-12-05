package nu.com.rill.analysis.report.excel.export;

import java.io.IOException;
import java.util.HashMap;
import java.util.Map;

import nu.com.rill.analysis.report.excel.ReportEngine;

import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.zkoss.poi.ss.usermodel.Workbook;

@ContextConfiguration(value="classpath:/nu/com/rill/analysis/report/excel/exporter/op-applicationContext-re-mail.xml")
public class OPInlineMailContentExporterTests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private SendReportViaEmailHelper sendReportViaEmailHelper;
	
	private static final String MODULE_NAME = "crm_tomcat_weihu";
	private static Map<String, String> CN_EN_NAMES = new HashMap<String, String>();
	static {
		CN_EN_NAMES.put("crm_tomcat_kt", "增值业绩追踪系统");
		CN_EN_NAMES.put("crm_tomcat_pangu", "盘古呼出系统");
		CN_EN_NAMES.put("crm_tomcat_pss", "智优系统");
		CN_EN_NAMES.put("crm_tomcat_pulse", "Pulse系统");
		CN_EN_NAMES.put("crm_tomcat_weihu", "维护系统");
	}
	private static final String SELECTED_DATE = "2012-12-04";
	
	private static Map<String, String> contextParams = new HashMap<String, String>();
	
	@BeforeClass
	public static void before() {
		
		contextParams.put(ReportEngine.URL, "jdbc:mysql://db-rigel-dev00.db01.baidu.com:8556/crmdb");
		contextParams.put("moduleName", MODULE_NAME);
		contextParams.put("moduleCnName", CN_EN_NAMES.get(MODULE_NAME));
		contextParams.put("selectedDate", SELECTED_DATE);
		contextParams.put(ReportEngine.SYSTEM_VIEW_PAGE, "http://ai-rigel-prd00.ai01.baidu.com:8080/_report/view2.zul?");
		
//		System.setProperty("re.mail.offlineMode", "false");
		
		// FIXME: MENGRAN. Where to set?
		System.setProperty("mail.mime.encodefilename", "true");
		
	}
	
	@Test
	public void byMachine() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/exporter/accesscnt-daily-bymachine.xlsx");
		try {
			
			Workbook wb = re.generateReport(cpr.getInputStream(), "accesscnt-daily-bymachine.xlsx", contextParams);
			HtmlExporter exporter = new HtmlExporter(wb, "accesscnt-daily-bymachine.xlsx", contextParams);
			
			// Start send via e-mail
			sendReportViaEmailHelper.export(exporter);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}
	
	@Test
	public void byStatus() {
		
		ReportEngine re = ReportEngine.INSTANCE;
		
		ClassPathResource cpr = new ClassPathResource("nu/com/rill/analysis/report/excel/exporter/accesscnt-daily-bystatus.xlsx");
		try {
			
			Workbook wb = re.generateReport(cpr.getInputStream(), "accesscnt-daily-bystatus.xlsx", contextParams);
			HtmlExporter exporter = new HtmlExporter(wb, "accesscnt-daily-bystatus.xlsx", contextParams);
			
			// Start send via e-mail
			sendReportViaEmailHelper.export(exporter);
			
		} catch (IOException e) {
			throw new RuntimeException(e);
		}
		
	}

}
