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
package org.rill.bpm.common.mail.support;

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
