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
package org.rill.bpm.common.mail;

import java.util.Map;

import org.rill.bpm.common.mail.support.AttachmentWarper;
import org.springframework.mail.SimpleMailMessage;


/**
 * Template mail sender
 * <p>Please use {@link AttachmentWarper} and key {{@link #ATTACHMENT_KEY} to specify attachment，
 * <br>Support single object, array, collection.
 **/
public interface TemplateMailSender {

    String ATTACHMENT_KEY = TemplateMailSender.class.getName() + ".ATTACHMENT_KEY";

    String generateMailContent(String templatePath, Map<String, Object> model);

    void sendMimeMeesage(SimpleMailMessage mailSource, String templatePath, Map<String, Object> model);
}
