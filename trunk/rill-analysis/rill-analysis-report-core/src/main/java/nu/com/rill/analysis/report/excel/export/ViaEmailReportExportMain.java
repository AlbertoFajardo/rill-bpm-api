package nu.com.rill.analysis.report.excel.export;

import java.util.HashMap;
import java.util.Map;

import nu.com.rill.analysis.report.ReportExportService;

import org.springframework.remoting.rmi.RmiProxyFactoryBean;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

public class ViaEmailReportExportMain {

	/**
	 * @param args
	 */
	public static void main(String[] args) {
		
		// Parse argument.
		Assert.isTrue(args.length >= 3);
		String templateName = args[0];
		Assert.isTrue(templateName != null && templateName.trim().length() > 0);
		String mailParamsString = args[1];
		Map<String, String> mailParams = new HashMap<String, String>();
		if (StringUtils.hasText(mailParamsString)) {
			String[] pair = StringUtils.delimitedListToStringArray(mailParamsString, ",");
			for (String s : pair) {
				Assert.isTrue(s.split("=").length == 2);
				mailParams.put(s.split("=")[0], s.split("=")[1]);
			}
		}
		
		Map<String, String> contextParams = new HashMap<String, String>();
		String contextParamsString = args[2];
		if (StringUtils.hasText(contextParamsString)) {
			String[] pair = StringUtils.delimitedListToStringArray(contextParamsString, ",");
			for (String s : pair) {
				Assert.isTrue(s.split("=").length == 2);
				contextParams.put(s.split("=")[0], s.split("=")[1]);
			}
		}
		
		String url = "rmi://ai-rigel-prd00.ai01.baidu.com:8110/ViaEmailReportExportService";
		if (args.length > 3) {
			url = args[3];
		}
		try {
			RmiProxyFactoryBean bean = new RmiProxyFactoryBean();
			bean.setServiceUrl(url);
			bean.setServiceInterface(ReportExportService.class);
			bean.afterPropertiesSet();
			
			ReportExportService service = (ReportExportService) bean.getObject();
			
			System.out.println("Send with params " + templateName + " " + ObjectUtils.getDisplayString(mailParams) + " " + ObjectUtils.getDisplayString(contextParams));
			service.export(templateName, mailParams, contextParams);
		} catch (Exception e) {
			e.printStackTrace();
			// TODO
		}

	}

}
