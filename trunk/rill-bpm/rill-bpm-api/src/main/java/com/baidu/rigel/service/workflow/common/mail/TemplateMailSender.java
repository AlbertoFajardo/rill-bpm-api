package com.baidu.rigel.service.workflow.common.mail;

import java.util.Map;

import org.springframework.mail.SimpleMailMessage;

import com.baidu.rigel.service.workflow.common.mail.support.AttachmentWarper;

/**
 * 公司名：百度 <br>
 * 系统名：Rigel直销系统<br>
 * 子系统名: <br>
 * 模块名：HT-SUPPORT <br>
 * 文件名：TemplateMailSender.java<br>
 * 功能说明: 基于模板的邮件发送支持类<br>
 * <p>如果需要发送附件，请使用{@link AttachmentWarper}以{{@link #ATTACHMENT_KEY} 放入model中，
 * <br>此类会自行判断。支持的类型为单个对象，对象数组，对象集合
 * 
 * @author rillmeng
 * @version 1.1.12
 * @date 2010-4-20下午08:39:31
**/
public interface TemplateMailSender {

	String ATTACHMENT_KEY = TemplateMailSender.class.getName() + ".ATTACHMENT_KEY";
	
	String generateMailContent(String templatePath, Map<String, Object> model);
	
	void sendMimeMeesage(SimpleMailMessage mailSource, String templatePath, Map<String, Object> model);
}
