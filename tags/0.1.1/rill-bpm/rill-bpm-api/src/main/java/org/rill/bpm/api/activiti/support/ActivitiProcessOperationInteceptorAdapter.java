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
package org.rill.bpm.api.activiti.support;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.ProcessOperationInteceptor;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.exception.ProcessException;
import org.springframework.util.StringUtils;

/**
 * Activiti engine process create interceptor adapter.
 * @author mengran
 **/
public abstract class ActivitiProcessOperationInteceptorAdapter implements
        ProcessOperationInteceptor {

    /** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass().getName());

    public abstract WorkflowOperations.PROCESS_OPERATION_TYPE handleOpeationType();

    public void postOperation(String engineProcessInstanceId) throws ProcessException {

        try {
            logger.debug("Execute process operation interceptor#postOperation " + this.getClass().getName());
            doPostOperation(engineProcessInstanceId);
        } catch (Exception e) {
            throw new ProcessException(e.getMessage());
        }
    }

    protected void doPostOperation(String engineProcessInstanceId) {
        // Do nothing
    }

    public final void preOperation(String engineProcessInstanceId,
            String operator, String reason) throws ProcessException {

        if (engineProcessInstanceId == null || !StringUtils.hasText(operator)) {
            throw new ProcessException("参数无效");
        }

        try {
            logger.debug("Execute process operation interceptor#preOperation " + this.getClass().getName());
            doPreOperation(engineProcessInstanceId, operator, reason);
        } catch (Exception e) {
            throw new ProcessException(e.getMessage(), e);
        }
    }

    protected void doPreOperation(String engineProcessInstanceId,
            String operator, String reason) {
        // expose method to client
        // Do nothing
    }
}
