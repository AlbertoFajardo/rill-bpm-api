/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.rill.bpm.common.mail.freemarker;

import java.beans.PropertyDescriptor;
import java.lang.reflect.Method;
import java.lang.reflect.Modifier;
import java.net.InetAddress;
import java.util.ArrayList;
import java.util.List;
import java.util.Map;

import javax.mail.internet.MimeMessage;

import org.rill.bpm.common.mail.TemplateMailSender;
import org.rill.bpm.common.mail.support.TemplateMailSenderSupport;
import org.springframework.beans.BeanUtils;
import org.springframework.beans.FatalBeanException;
import org.springframework.mail.MailMessage;
import org.springframework.mail.SimpleMailMessage;
import org.springframework.mail.javamail.MimeMessageHelper;
import org.springframework.ui.freemarker.FreeMarkerTemplateUtils;
import org.springframework.util.ObjectUtils;

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
			logger.error("Fail to process mail template.", e);
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
		
		// Start to set properties
		final MimeMessageHelper finalMessageHelper;
		try {
			
			MimeMessage mimeMessage = getMailSender().createMimeMessage();
			MimeMessageHelper messageHelper = new MimeMessageHelper(mimeMessage, isMultipart(model), getEncode());
			finalMessageHelper = messageHelper;
			
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
			
			// Prepare mail information
			messageHelper = processMultipart(model, messageHelper);

		} catch (Exception e) {
			throw new RuntimeException(e);
		}
		
		// Do send mail
		getTaskExecutor().execute(new Runnable() {
			public void run() {
				getMailSender().send(finalMessageHelper.getMimeMessage());
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
