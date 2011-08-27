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
package com.baidu.rigel.service.workflow.api.activiti.support;

import com.baidu.rigel.service.workflow.api.ProcessCreateInteceptor;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.runtime.ProcessInstance;
import org.apache.commons.lang.StringUtils;

/**
 * Activiti engine process create interceptor adapter.
 * @author mengran
 */
public abstract class ActivitiProcessCreateInterceptorAdapter implements ProcessCreateInteceptor {

    /** Logger available to subclasses */
    protected final static Logger logger = Logger.getLogger(ActivitiProcessCreateInterceptorAdapter.class.getName());
    
    private RuntimeService runtimeService;

    public void setRuntimeService(RuntimeService runtimeService) {
        this.runtimeService = runtimeService;
    }

    private void checkParam(String processDefinitionKey, String processStarter) {

        if (StringUtils.isEmpty(processDefinitionKey)) {
            throw new ProcessException("modelInfo must a String.");
        }
    }

    public final void preOperation(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) throws ProcessException {

        // Check context parameter
        checkParam(processDefinitionKey, processStarter);

        try {
            logger.log(Level.FINE, "Execute process create interceptor#preOperation [{0}].", this);
            doPreOperation(processDefinitionKey, processStarter, businessObjectId, startParams);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    public final void postOperation(String engineProcessInstanceId, String businessObjectId, String processStarter) throws ProcessException {

        // Check context parameter
        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceId(engineProcessInstanceId).singleResult();
        if (!(processInstance instanceof ProcessInstance)) {
            throw new ProcessException("processInstance must a " + ProcessInstance.class.getName() + ".");
        }

        try {
            logger.log(Level.FINE, "Execute process create interceptor#postOperation [{0}].", this);
            // FIXME: Need proxy it for prevent call some method.
            doPostOperation(processInstance, businessObjectId, processStarter);
        } catch (Exception e) {
            throw new ProcessException(e);
        }
    }

    protected String doPreOperation(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) {

        return processDefinitionKey;
    }

    protected void doPostOperation(ProcessInstance engineProcessInstance, String businessObjectId, String processStarter) {

        // Do nothing
    }
}
