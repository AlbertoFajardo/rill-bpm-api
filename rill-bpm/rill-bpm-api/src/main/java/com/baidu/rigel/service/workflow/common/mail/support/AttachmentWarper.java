package com.baidu.rigel.service.workflow.common.mail.support;

import org.springframework.core.io.InputStreamSource;

public class AttachmentWarper {

	private String attachmentFileName;
	private String contentType;
	private InputStreamSource inputStreamSource;
	
	
	public AttachmentWarper(String attachmentFileName, String contentType,
			InputStreamSource inputStreamSource) {
		super();
		this.attachmentFileName = attachmentFileName;
		this.contentType = contentType;
		this.inputStreamSource = inputStreamSource;
	}
	/**
	 * @return the attachmentFileName
	 */
	public String getAttachmentFileName() {
		return attachmentFileName;
	}
	/**
	 * @param attachmentFileName the attachmentFileName to set
	 */
	public void setAttachmentFileName(String attachmentFileName) {
		this.attachmentFileName = attachmentFileName;
	}
	/**
	 * @return the contentType
	 */
	public String getContentType() {
		return contentType;
	}
	/**
	 * @param contentType the contentType to set
	 */
	public void setContentType(String contentType) {
		this.contentType = contentType;
	}
	/**
	 * @return the inputStreamSource
	 */
	public InputStreamSource getInputStreamSource() {
		return inputStreamSource;
	}
	/**
	 * @param inputStreamSource the inputStreamSource to set
	 */
	public void setInputStreamSource(InputStreamSource inputStreamSource) {
		this.inputStreamSource = inputStreamSource;
	}
}
