package nu.com.rill.analysis.report.excel.export;

import java.util.Map;
import java.util.Map.Entry;

import org.rill.bpm.common.mail.freemarker.FreeMarkerTemplateMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;
import org.zkoss.zk.ui.Executions;

public class InlineMailContentMailSender extends FreeMarkerTemplateMailSender {

	public static final String MAIL_CONTENT_KEY = InlineMailContentMailSender.class.getName() + ".MAIL_CONTENT_KEY"; 
	
	@Override
	public String generateMailContent(String templatePath,
			Map<String, Object> model) {
		
		HtmlExporter exporter = (HtmlExporter) model.get(MAIL_CONTENT_KEY);
		String html = exporter.export();
		logger.debug("Original html : " + html);
		Assert.hasText(html, "Empty mail content is not allowed. " + templatePath);
		
		// FIXME: MENGRAN. Need use HTML-parser tools
		// Process in-line image
		for (Entry<String, byte[]> entry : exporter.getImages().entrySet()) {
			String cid = StringUtils.replace(entry.getKey(), ".", "_");
			html = StringUtils.replace(html, "./" + entry.getKey(), "cid:" + cid);
			try {
				String prefix = Executions.getCurrent().getContextPath() + "/images";
				html = StringUtils.replace(html, prefix + "/" + entry.getKey(), "cid:" + cid);
			} catch (Exception e) {
				// Ignore
			}
			logger.debug("After replace " + cid + " : " + html);
		}
		
		logger.debug("Mail content is : " + html);
		return html;
	}

	@Override
	public void sendMimeMeesage(SimpleMailMessage mailSource,
			String templatePath, Map<String, Object> model) {
		
		HtmlExporter exporter = (HtmlExporter) model.get(MAIL_CONTENT_KEY);
		String html = exporter.export();
		int startIndex = html.indexOf("<title>");
		int endIndex = html.indexOf("</title>");
		if (startIndex > 0) {
			String title = html.substring(startIndex + 7, endIndex);
			logger.debug("Retrieve title: " + title);
			mailSource.setSubject(title + mailSource.getSubject());
		}
		
		// Do super's logic
		super.sendMimeMeesage(mailSource, templatePath, model);
	}
	
	
	
}
