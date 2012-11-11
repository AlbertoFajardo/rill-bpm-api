package nu.com.rill.analysis.report.excel.export;

import java.util.Map;
import java.util.Map.Entry;

import org.rill.bpm.common.mail.freemarker.FreeMarkerTemplateMailSender;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

import com.hp.gagawa.java.Node;
import com.hp.gagawa.java.elements.Div;
import com.hp.gagawa.java.elements.Img;
import com.hp.gagawa.java.elements.Text;
import com.hp.gagawa.java.elements.Title;

public class InlineMailContentMailSender extends FreeMarkerTemplateMailSender {

	public static final String MAIL_CONTENT_KEY = InlineMailContentMailSender.class.getName() + ".MAIL_CONTENT_KEY"; 
	
	@Override
	public String generateMailContent(String templatePath,
			Map<String, Object> model) {
		
		HtmlExporter exporter = (HtmlExporter) model.get(MAIL_CONTENT_KEY);
		String html = exporter.export();
		logger.debug("Original html : " + html);
		Assert.hasText(html, "Empty mail content is not allowed. " + templatePath);
		
		// Process DIV's position
		for (Entry<String, Div> entry : exporter.getImageHolders().entrySet()) {
			Img img = (Img) entry.getValue().getChild(0);
			String cid = StringUtils.replace(entry.getKey(), ".", "_");
			img.setSrc("cid:" + cid);
			exporter.getBody().removeChild(entry.getValue());
			String vml = "<v:image src=\"" + img.getSrc() + "\" style=\"" + entry.getValue().getStyle() + "\"/>";
			Text vimText = new Text(vml);
			exporter.getBody().appendChild(0, vimText);
		}
		
		// Re-export it.
		html = exporter.export();
		logger.debug("Mail content is : " + html);
		return html;
	}

	@Override
	public void sendMimeMeesage(SimpleMailMessage mailSource,
			String templatePath, Map<String, Object> model) {
		
		HtmlExporter exporter = (HtmlExporter) model.get(MAIL_CONTENT_KEY);
		for (Node n : exporter.getHead().getChildren()) {
			if (n instanceof Title) {
				Text text = (Text) ((Title) n).getChild(0);
				logger.debug("Retrieve title: " + text.toString());
				mailSource.setSubject(text.toString() + mailSource.getSubject());
			}
		}
		
		// Do super's logic
		super.sendMimeMeesage(mailSource, templatePath, model);
	}
	
	
	
}
