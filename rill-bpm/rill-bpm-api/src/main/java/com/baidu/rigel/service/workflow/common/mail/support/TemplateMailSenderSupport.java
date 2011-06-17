package com.baidu.rigel.service.workflow.common.mail.support;

import java.util.ArrayList;
import java.util.Collection;
import java.util.List;
import java.util.Map;

import javax.mail.MessagingException;
import javax.mail.internet.MimeMessage;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.core.task.TaskExecutor;
import org.springframework.mail.javamail.JavaMailSender;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.util.Assert;

import com.baidu.rigel.service.workflow.common.mail.TemplateMailSender;

public class TemplateMailSenderSupport {

	public static final String DEFAULT_ENCODE = "UTF-8";
	protected static final Log logger = LogFactory.getLog(TemplateMailSenderSupport.class);
	
	private JavaMailSender mailSender;
	private TaskExecutor taskExecutor;
	private String encode = DEFAULT_ENCODE;

	/**
	 * @return the encode
	 */
	public String getEncode() {
		return encode;
	}

	/**
	 * @param encode the encode to set
	 */
	public void setEncode(String encode) {
		this.encode = encode;
		Assert.hasLength(this.encode, "Encode is mismatch");
	}

	/**
	 * @return the mailSender
	 */
	public JavaMailSender getMailSender() {
		return mailSender;
	}

	/**
	 * @param mailSender the mailSender to set
	 */
	public void setMailSender(JavaMailSender mailSender) {
		this.mailSender = mailSender;
	}

	/**
	 * @return the taskExecutor
	 */
	public TaskExecutor getTaskExecutor() {
		return taskExecutor;
	}

	/**
	 * @param taskExecutor the taskExecutor to set
	 */
	public void setTaskExecutor(TaskExecutor taskExecutor) {
		this.taskExecutor = taskExecutor;
	}

	protected final boolean isMultipart(Map<String, Object> model) {
		
		if (model.containsKey(TemplateMailSender.ATTACHMENT_KEY)) {
			return true;
		}
		
		return false;
	}
	
	@SuppressWarnings("unchecked")
	protected MimeMessageHelper obtainMimeMessageHelper(Map<String, Object> model) throws MessagingException {
		
		MimeMessage mimeMessage = getMailSender().createMimeMessage();
		MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, isMultipart(model), getEncode());
		
		// Handle multipart
		if (isMultipart(model)) {
			List<AttachmentWarper> list = new ArrayList<AttachmentWarper>();
			Object attachments = model.get(TemplateMailSender.ATTACHMENT_KEY);
			if (attachments.getClass().isArray()) {
				AttachmentWarper[] awArrays = ((AttachmentWarper[]) attachments);
				for (AttachmentWarper aw : awArrays) {
					list.add(aw);
				}
			} else if (attachments instanceof Collection) {
				Collection<AttachmentWarper> c = (Collection<AttachmentWarper>) attachments;
				list.addAll(c);
			} else {
				list.add(((AttachmentWarper) attachments));
			}
			
			for (AttachmentWarper aw : list) {
				if (aw.getContentType() == null)
					messageHelper.addAttachment(aw.getAttachmentFileName(), aw.getInputStreamSource());
				else
					messageHelper.addAttachment(aw.getAttachmentFileName(), aw.getInputStreamSource(), aw.getContentType());
			}
		}
		
		return messageHelper;
	}
	
}
