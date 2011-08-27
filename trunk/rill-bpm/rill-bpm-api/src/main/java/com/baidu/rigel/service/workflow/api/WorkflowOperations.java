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
package com.baidu.rigel.service.workflow.api;

import java.util.LinkedHashMap;
import java.util.Map;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.util.Set;

/**
 * Workflow operations abstraction, it define all interface of actual engine.
 * 
 * @author mengran
 */
public interface WorkflowOperations {

    // ------------------------------------ Process related API ------------------------------------ //
    enum PROCESS_OPERATION_TYPE {

        SUSPEND, // Suspend operation
        RESUME, // Resume operation
        TERMINAL // Terminal operation
    }

    /**
     * Start a process instance.
     * @param processDefinitionKey Process definition informations
     * @param processStarter Process starter informations
     * @param businessObjectId Business object ID <code>NOT NULL</code>
     * @param startParams Start parameters for calculate transition if need
     * @throws ProcessException Exception occurred during creation
     */
    void createProcessInstance(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) throws ProcessException;

    /**
     * Terminal process instance
     * @param engineProcessInstanceId engine process instance ID
     * @param operator operator
     * @param reason Reason for operation
     */
    void terminalProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException;

    /**
     * Suspend process instance
     * @param engineProcessInstanceId engine process instance ID
     * @param operator operator
     * @param reason Reason for operation
     */
    void suspendProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException;

    /**
     * Resume process instance
     * @param engineProcessInstanceId engine process instance ID
     * @param operator operator
     * @param reason Reason for operation
     */
    void resumeProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException;
    
    
    // ------------------------------------ Task related API ------------------------------------ //
    /**
     * Task form data key. 
     */
    String ENGINE_DRIVEN_TASK_FORM_DATA_KEY = WorkflowOperations.class.getName() + ".ENGINE_DRIVEN_TASK_FORM_DATA_KEY";
    /**
     * Task return data key.
     */
    String ENGINE_DRIVEN_TASK_RETURN_DATA_KEY = WorkflowOperations.class.getName() + ".ENGINE_DRIVEN_TASK_RETURN_DATA_KEY";

    /**
     * Complete task instance
     * @param engineTaskInstanceId engine task instance ID
     * @param operator operator
     * @param workflowParams Operation parameter for calculate transition if need
     */
    void completeTaskInstance(String engineTaskInstanceId, String operator, Map<String, Object> workflowParams) throws ProcessException;

    /**
     * Batch complete task instances
     * @param batchDTO DTO<EngineTaskInstanceID, WorkflowParams>
     * @param opeartor Operator
     */
    void batchCompleteTaskIntances(LinkedHashMap<String, Map<String, Object>> batchDTO, String opeartor) throws ProcessException;

    /**
     * Obtain task instance extend attribute
     * @param engineTaskInstanceId engine task instance
     * @return extend attributes of task
     */
    Map<String, String> getTaskInstanceExtendAttrs(String engineTaskInstanceId);

    /**
     * Get task name by given define ID.
     * @param processDefinitionKey  process definition key
     * @param taskDefineId task define ID
     * @return task name
     */
    String getTaskNameByDefineId(String processDefinitionKey, String taskDefineId);

    /**
     * Get process instance's variables
     * @param engineProcessInstanceId process instance ID(NOT NULL)
     * @return process instance's variables
     */
    Set<String> getProcessInstanceVariableNames(String engineProcessInstanceId);

    /**
     * @deprecated by MENGRAN at 2011-08-27
     * Abort task instance.
     * <p>
     *  Please refer engine-spec API Document for details.<br>
     * @param engineTaskInstanceId
     * @throws ProcessException Exception occurred during creation
     */
    void abortTaskInstance(String engineTaskInstanceId) throws ProcessException;

    /**
     * Obtain task executer role
     * @param engineTaskInstanceId engine task instance ID
     * @return task executer role
     * @throws ProcessException Exception occurred during creation
     */
    String obtainTaskRole(String engineTaskInstanceId) throws ProcessException;

    /**
     * Reassign task executer
     * @param engineProcessInstanceId engine process instance ID
     * @param engineTaskInstanceId engine task instance ID
     * @param oldExecuter original executer
     * @param newExecuter new executer
     */
    void reassignTaskExecuter(String engineProcessInstanceId, String engineTaskInstanceId, String oldExecuter, String newExecuter) throws ProcessException;

}
