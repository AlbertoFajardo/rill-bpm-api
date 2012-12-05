package nu.com.rill.analysis.report.excel.export;

import java.io.ByteArrayOutputStream;
import java.io.IOException;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.common.mail.TemplateMailSender;
import org.rill.bpm.common.mail.support.AttachmentWarper;
import org.springframework.core.io.ByteArrayResource;
import org.springframework.mail.SimpleMailMessage;

import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Title;

public class SendReportViaEmailHelper {

	public static final Log LOGGER = LogFactory.getLog(SendReportViaEmailHelper.class);
	
	private TemplateMailSender templateMailSender;
	private SimpleMailMessage simpleMailMessage;

	public SimpleMailMessage getSimpleMailMessage() {
		return simpleMailMessage;
	}

	public void setSimpleMailMessage(SimpleMailMessage simpleMailMessage) {
		this.simpleMailMessage = simpleMailMessage;
	}

	public TemplateMailSender getTemplateMailSender() {
		return templateMailSender;
	}

	public void setTemplateMailSender(TemplateMailSender templateMailSender) {
		this.templateMailSender = templateMailSender;
	}
	
	public void export(HtmlExporter htmlExporter) {
		
		htmlExporter.export();
		Map<String, Object> model = new HashMap<String, Object>();
		model.put(InlineMailContentMailSender.MAIL_CONTENT_KEY, htmlExporter);
		
		// Add inline
		List<AttachmentWarper> inline = new ArrayList<AttachmentWarper>();
		for (Entry<String, byte[]> entry : htmlExporter.getImages().entrySet()) {
			String cid = entry.getKey(); //StringUtils.replace(entry.getKey(), ".", "_");
			LOGGER.debug("Add inline cid " + cid);
			AttachmentWarper aw = new AttachmentWarper(cid, null, new ByteArrayResource(entry.getValue()));
			inline.add(aw);
		}
		model.put(TemplateMailSender.INLINE_KEY, inline);
		
		// Add attachment
		List<AttachmentWarper> attachment = new ArrayList<AttachmentWarper>();
		ByteArrayOutputStream baos = new ByteArrayOutputStream();
		String attachementName = htmlExporter.getWorkBookName();
		try {
			for (Node n : htmlExporter.getHead().getChildren()) {
				if (n instanceof Title) {
					Text text = (Text) ((Title) n).getChild(0);
					String suffix = attachementName.indexOf('.') < 0 ? "" : attachementName.substring(attachementName.indexOf('.'));
					attachementName = text.toString() + suffix;
				}
			}
		} catch (Exception e) {
			LOGGER.warn("Error occurred when try to retrieve attachment name, so use workbook name " + htmlExporter.getWorkBookName(), e);
		}
		try {
			htmlExporter.getWb().write(baos);
			AttachmentWarper att = new AttachmentWarper(attachementName, null, new ByteArrayResource(baos.toByteArray()));
			attachment.add(att);
		} catch (IOException e) {
			LOGGER.warn("Error occurred when try to add attachemet " + htmlExporter.getWorkBookName(), e);
		}
		model.put(TemplateMailSender.ATTACHMENT_KEY, attachment);
		
		// Build send mail information
		SimpleMailMessage sendUse = new SimpleMailMessage();
		simpleMailMessage.copyTo(sendUse);
		
		templateMailSender.sendMimeMeesage(sendUse, htmlExporter.getWorkBookName(), model);
	}
	
}
