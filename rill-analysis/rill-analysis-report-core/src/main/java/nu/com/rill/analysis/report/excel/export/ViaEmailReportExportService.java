package nu.com.rill.analysis.report.excel.export;

import java.io.ByteArrayOutputStream;
import java.io.InputStream;
import java.util.Iterator;
import java.util.Map;
import java.util.ServiceLoader;

import nu.com.rill.analysis.report.REException;
import nu.com.rill.analysis.report.ReportExportService;
import nu.com.rill.analysis.report.ReportTemplateRetriever;

import org.apache.commons.io.IOUtils;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.core.io.ClassPathResource;
import org.springframework.core.io.Resource;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class ViaEmailReportExportService implements BeanFactoryAware, InitializingBean, ReportExportService {

	private BeanFactory beanFactory;
	private ReportTemplateRetriever reportTemplateRetriever;

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		
		this.beanFactory = beanFactory; 
	}
	
	@Override
	public void afterPropertiesSet() throws Exception {
		
		ServiceLoader<ReportTemplateRetriever> sl = ServiceLoader.load(ReportTemplateRetriever.class);
		Iterator<ReportTemplateRetriever> ps = sl.iterator();
		while (ps.hasNext()) {
			ReportTemplateRetriever rtr = ps.next();
			if (rtr != null) {
				reportTemplateRetriever = rtr;
			}
		}
		
		if (reportTemplateRetriever == null) {
			reportTemplateRetriever = new ReportTemplateRetriever() {
				
				private String classPath = ViaEmailReportExportService.class.getName()
						.replace("." + ViaEmailReportExportService.class.getSimpleName(), "")
						.replace(".", "/");
				
				@Override
				public byte[] retrieveReportTemplate(String reportName) {
					
					ClassPathResource cpr = new ClassPathResource(classPath + "/" + reportName);
					try {
						InputStream is = cpr.getInputStream();
						ByteArrayOutputStream baos = new ByteArrayOutputStream();
						IOUtils.copy(is, baos);
						IOUtils.closeQuietly(is);
						
						return baos.toByteArray();
					} catch (Exception e) {
						throw new REException(e);
					}
				}
			};
		}
	}

	protected SimpleMailMessage buildMailMessage(SimpleMailMessage ori, Map<String, String> mailParams) {
		
		Assert.notNull(ori);
		Assert.notNull(mailParams);
		
		if (mailParams.containsKey("from")) {
			ori.setFrom(mailParams.get("from"));
		}
		if (mailParams.containsKey("cc")) {
			ori.setCc(StringUtils.commaDelimitedListToStringArray(mailParams.get("cc")));
		}
		if (mailParams.containsKey("bcc")) {
			ori.setBcc(StringUtils.commaDelimitedListToStringArray(mailParams.get("bcc")));
		}
		if (mailParams.containsKey("to")) {
			ori.setTo(StringUtils.commaDelimitedListToStringArray(mailParams.get("to")));
		}
		if (mailParams.containsKey("subject")) {
			ori.setSubject(mailParams.get("subject") + ori.getSubject());
		}
		
		return ori;
	}
	
	protected Resource retrieveReport(String reportName) {
		
		ByteArrayResource bar = new ByteArrayResource(reportTemplateRetriever.retrieveReportTemplate(reportName), reportName);
		
		return bar;
	}

	public void export(String reportName, Map<String, String> mailParams, Map<String, String> reportParams) {
		
		SendReportViaEmailHelper helper = this.beanFactory.getBean(SendReportViaEmailHelper.class);
		helper.setSimpleMailMessage(buildMailMessage(helper.getSimpleMailMessage(), mailParams));
		
		helper.export(retrieveReport(reportName), reportParams);
	}
}
