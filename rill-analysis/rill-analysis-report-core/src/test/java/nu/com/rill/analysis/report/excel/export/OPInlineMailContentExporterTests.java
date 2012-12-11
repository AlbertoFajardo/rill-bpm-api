package nu.com.rill.analysis.report.excel.export;

import java.text.SimpleDateFormat;
import java.util.Date;
import java.util.HashMap;
import java.util.Map;

import nu.com.rill.analysis.report.ReportExportService;
import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.lang.time.DateUtils;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.util.Assert;

public class OPInlineMailContentExporterTests {

	private static final String MODULE_NAME = "crm_tomcat_kt";
	private static final String MODULE_TOPN = "50";
	private static Map<String, String> CN_EN_NAMES = new HashMap<String, String>();
	private static Map<String, String> MODULE_THRESHOLDS = new HashMap<String, String>();
	static {
		CN_EN_NAMES.put("crm_tomcat_kt", "增值业绩追踪系统");
		CN_EN_NAMES.put("crm_tomcat_pangu", "盘古呼出系统");
		CN_EN_NAMES.put("crm_tomcat_pss", "智优系统");
		CN_EN_NAMES.put("crm_tomcat_pulse", "Pulse系统");
		CN_EN_NAMES.put("crm_tomcat_weihu", "维护系统");
		
		MODULE_THRESHOLDS.put("crm_tomcat_kt", "1");
		MODULE_THRESHOLDS.put("crm_tomcat_pangu", "15");
		MODULE_THRESHOLDS.put("crm_tomcat_pss", "8");
		MODULE_THRESHOLDS.put("crm_tomcat_pulse", "1");
		MODULE_THRESHOLDS.put("crm_tomcat_weihu", "5");
	}
	private static final String SELECTED_DATE = new SimpleDateFormat("yyyy-MM-dd").format(DateUtils.addDays(new Date(), -1));
	
	private static Map<String, String> contextParams = new HashMap<String, String>();
	
	@BeforeClass
	public static void before() {
		
		contextParams.put(ReportEngine.URL, "jdbc:mysql://db-rigel-dev00.db01.baidu.com:8556/crmdb");
		contextParams.put("moduleName", MODULE_NAME);
		contextParams.put("topN", MODULE_TOPN);
		contextParams.put("threshold", MODULE_THRESHOLDS.get(MODULE_NAME));
		contextParams.put("moduleCnName", CN_EN_NAMES.get(MODULE_NAME));
		contextParams.put("selectedDate", SELECTED_DATE);
		contextParams.put(ReportEngine.SYSTEM_VIEW_PAGE, "http://ai-rigel-prd00.ai01.baidu.com:8080/_report/view2.zul?");
		
//		System.setProperty("re.mail.offlineMode", "false");
		
		// FIXME: MENGRAN. Where to set?
		System.setProperty("mail.mime.encodefilename", "true");
		
	}
	
	@Test
	public void byMachine() {
		
		Assert.notNull(ReportEngine.INSTANCE);
		
		RmiProxyFactoryBean bean = new RmiProxyFactoryBean();
		bean.setServiceUrl("rmi://localhost:8110/ViaEmailReportExportService");
		bean.setServiceInterface(ReportExportService.class);
		bean.afterPropertiesSet();
		
		ReportExportService service = (ReportExportService) bean.getObject();
		
		Map<String, String> mailParams = new HashMap<String, String>();
		mailParams.put("from", "watchdog@baidu.com");
		mailParams.put("to", "sfcrm-mon@baidu.com,Rigel-op@baidu.com");
		service.export("accesscnt-daily-bymachine.xlsx", mailParams, contextParams);
		
	}
	
	@Test
	public void byStatus() {
		
		Assert.notNull(ReportEngine.INSTANCE);
		
		RmiProxyFactoryBean bean = new RmiProxyFactoryBean();
		bean.setServiceUrl("rmi://localhost:8110/ViaEmailReportExportService");
		bean.setServiceInterface(ReportExportService.class);
		bean.afterPropertiesSet();
		
		ReportExportService service = (ReportExportService) bean.getObject();
		
		Map<String, String> mailParams = new HashMap<String, String>();
		mailParams.put("from", "watchdog@baidu.com");
		mailParams.put("to", "sfcrm-mon@baidu.com,Rigel-op@baidu.com");
		service.export("accesscnt-daily-bystatus.xlsx", mailParams, contextParams);
		
	}
	
	@Test
	public void bySecond() {
		
		Assert.notNull(ReportEngine.INSTANCE);
		
		RmiProxyFactoryBean bean = new RmiProxyFactoryBean();
		bean.setServiceUrl("rmi://localhost:8110/ViaEmailReportExportService");
		bean.setServiceInterface(ReportExportService.class);
		bean.afterPropertiesSet();
		
		ReportExportService service = (ReportExportService) bean.getObject();
		
		Map<String, String> mailParams = new HashMap<String, String>();
		mailParams.put("from", "watchdog@baidu.com");
		mailParams.put("to", "sfcrm-mon@baidu.com,Rigel-op@baidu.com");
		service.export("accesscnt-daily-bysecond.xlsx", mailParams, contextParams);
		
	}

}
