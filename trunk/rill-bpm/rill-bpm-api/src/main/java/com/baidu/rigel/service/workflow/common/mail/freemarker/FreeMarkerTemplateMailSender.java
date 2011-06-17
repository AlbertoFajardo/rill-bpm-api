package com.baidu.rigel.service.workflow.common.mail.freemarker;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.ObjectUtils;

import com.baidu.rigel.service.workflow.common.mail.TemplateMailSender;
import com.baidu.rigel.service.workflow.common.mail.support.TemplateMailSenderSupport;

import freemarker.template.Configuration;
import freemarker.template.Template;

public class FreeMarkerTemplateMailSender extends TemplateMailSenderSupport implements
		TemplateMailSender {

	private Configuration freeMarkerConfigurerSupport;

	/**
	 * @return the freeMarkerConfigurerSupport
	 */
	public Configuration getFreeMarkerConfigurerSupport() {
		return freeMarkerConfigurerSupport;
	}

	/**
	 * @param freeMarkerConfigurerSupport the freeMarkerConfigurerSupport to set
	 */
	public void setFreeMarkerConfigurerSupport(
			Configuration freeMarkerConfigurerSupport) {
		this.freeMarkerConfigurerSupport = freeMarkerConfigurerSupport;
	}

	public String generateMailContent(String templatePath,
			Map<String, Object> model) {
		
		try {
			Template template = freeMarkerConfigurerSupport.getTemplate(templatePath);
			// generate using model
			return FreeMarkerTemplateUtils.processTemplateIntoString(template, model);
		} catch (Exception e) {
			if (logger.isErrorEnabled()) {
				logger.error(e.getMessage(), e.getCause());
			}
			throw new RuntimeException(e);
		}
	}

	public void sendMimeMeesage(SimpleMailMessage mailSource, String templatePath,
			Map<String, Object> model) {
		
		try {
			InetAddress localHostInfo = InetAddress.getLocalHost();
			model.put("hostInfo", localHostInfo.getHostName() + " " + localHostInfo.getHostAddress());
		} catch(Exception e) {
			model.put("hostInfo", "unknown");
		}
		
		final MimeMessageHelper messageHelper;
		// Start to set properties
		try {
			// Prepare mail information
			messageHelper = obtainMimeMessageHelper(model);
			
			// Generate mail content
			String content = generateMailContent(templatePath, model);
			mailSource.setText(content);
			
			if (mailSource.getFrom() != null) {
				messageHelper.setFrom(mailSource.getFrom());
			}
			if (mailSource.getSubject() != null) {
				messageHelper.setSubject(mailSource.getSubject());
			}
			if (!ObjectUtils.isEmpty(mailSource.getTo())) {
				messageHelper.setTo(mailSource.getTo());
			}
			if (!ObjectUtils.isEmpty(mailSource.getCc())) {
				messageHelper.setCc(mailSource.getCc());
			}
			if (!ObjectUtils.isEmpty(mailSource.getBcc())) {
				messageHelper.setBcc(mailSource.getBcc());
			}
			if (mailSource.getText() != null) {
				messageHelper.setText(mailSource.getText(), true);
			}
		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// Do send mail
		getTaskExecutor().execute(new Runnable() {
			public void run() {
				getMailSender().send(messageHelper.getMimeMessage());
			}
		});
	}
	
	/**
	 * Find NULL getter method and add it to ignore list
	 * @param mailSource source
	 * @return ignore properties list
	 */
	protected String[] getIgnoreProperties(MailMessage mailSource) {
		
		List<String> ignoreList = new ArrayList<String>();
		PropertyDescriptor[] propertyDescriptor = BeanUtils.getPropertyDescriptors(mailSource.getClass());
		for (int i = 0; i < propertyDescriptor.length; i++) {
			PropertyDescriptor targetPd = propertyDescriptor[i];
			if (targetPd.getWriteMethod() != null) {
				PropertyDescriptor sourcePd = BeanUtils.getPropertyDescriptor(mailSource.getClass(), targetPd.getName());
				if (sourcePd != null && sourcePd.getReadMethod() != null) {
					try {
						Method readMethod = sourcePd.getReadMethod();
						if (!Modifier.isPublic(readMethod.getDeclaringClass().getModifiers())) {
							readMethod.setAccessible(true);
						}
						Object value = readMethod.invoke(mailSource, new Object[0]);
						if (value == null) {
							ignoreList.add(targetPd.getName());
						}
					}
					catch (Throwable ex) {
						throw new FatalBeanException("Could not copy properties from source to target", ex);
					}
				}
			}
		}
		
		return ignoreList.toArray(new String[ignoreList.size()]);
	}

}
