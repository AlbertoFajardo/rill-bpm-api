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

import java.util.Map;

import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import com.thoughtworks.xstream.XStream;
import java.util.HashMap;
import java.util.List;
import java.util.Set;
import javax.jws.WebService;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;
import org.springframework.util.Assert;

/**
 * Workflow operations abstraction, it define all interface of actual engine.
 * 
 * @author mengran
 */
@WebService
public interface WorkflowOperations {

    public static final class XStreamSerializeHelper {

        public static String serializeXml(String rootElement, Object target) {
            Assert.notNull(target);

            return serializeXml(rootElement, target, target.getClass());
        }

        public static String serializeXml(String rootElement, Object target, Class<?> targetClazz) {

            Assert.notNull(rootElement);
            Assert.notNull(target);
            Assert.notNull(targetClazz);

            XStream xstream = new XStream();
            xstream.alias(rootElement, targetClazz);

            return xstream.toXML(target);
        }
    }
    
    // ------------------------------------ Process related API ------------------------------------ //
    enum PROCESS_OPERATION_TYPE {

        SUSPEND, // Suspend operation
        RESUME, // Resume operation
        TERMINAL // Terminal operation
    }
    
    class MapElements {

        @XmlElement
        public String key;
        @XmlElement
        public String value;

        private MapElements() {
        } //Required by JAXB

        public MapElements(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    
    class MapAdapter extends XmlAdapter<MapElements[], Map<String, String>> {

        public MapElements[] marshal(Map<String, String> arg0) throws Exception {
            MapElements[] mapElements = new MapElements[arg0.size()];
            int i = 0;
            for (Map.Entry<String, String> entry : arg0.entrySet()) {
                mapElements[i++] = new MapElements(entry.getKey(), entry.getValue());
            }

            return mapElements;
        }

        public Map<String, String> unmarshal(MapElements[] arg0) throws Exception {
            Map<String, String> r = new HashMap<String, String>();
            for (MapElements mapelement : arg0) {
                r.put(mapelement.key, mapelement.value);
            }
            return r;
        }
    }
    
    @XmlRootElement
    class CreateProcessInstanceDto {
        
        String processDefinitionKey;
        String processStarter;
        String businessObjectId;
        Map<String, String> startParams;

        // Required by JAXB
        public CreateProcessInstanceDto() {
        }
        
        public CreateProcessInstanceDto(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, String> startParams) {
            this.processDefinitionKey = processDefinitionKey;
            this.processStarter = processStarter;
            this.businessObjectId = businessObjectId;
            this.startParams = startParams;
        }

        public String getBusinessObjectId() {
            return businessObjectId;
        }

        public void setBusinessObjectId(String businessObjectId) {
            this.businessObjectId = businessObjectId;
        }

        public String getProcessDefinitionKey() {
            return processDefinitionKey;
        }

        public void setProcessDefinitionKey(String processDefinitionKey) {
            this.processDefinitionKey = processDefinitionKey;
        }

        public String getProcessStarter() {
            return processStarter;
        }

        public void setProcessStarter(String processStarter) {
            this.processStarter = processStarter;
        }
        
        @XmlJavaTypeAdapter(MapAdapter.class)
        public Map<String, String> getStartParams() {
            return startParams;
        }

        public void setStartParams(Map<String, String> startParams) {
            this.startParams = startParams;
        }
    }
    
    /**
     * Start a process instance.
     * @param processDefinitionKey Process definition informations
     * @param processStarter Process starter informations
     * @param businessObjectId Business object ID <code>NOT NULL</code>
     * @param startParams Start parameters for calculate transition if need
     * @throws ProcessException Exception occurred during creation
     */
    void createProcessInstance(CreateProcessInstanceDto createProcessInstanceDto) throws ProcessException;

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

    @XmlRootElement
    class CompleteTaskInstanceDto {
        
        String engineTaskInstanceId;
        String operator;
        Map<String, String> workflowParams;

        // Required by JAXB
        public CompleteTaskInstanceDto() {
        }

        public CompleteTaskInstanceDto(String engineTaskInstanceId, String operator, Map<String, String> workflowParams) {
            this.engineTaskInstanceId = engineTaskInstanceId;
            this.operator = operator;
            this.workflowParams = workflowParams;
        }

        
        public String getEngineTaskInstanceId() {
            return engineTaskInstanceId;
        }

        public void setEngineTaskInstanceId(String engineTaskInstanceId) {
            this.engineTaskInstanceId = engineTaskInstanceId;
        }

        public String getOperator() {
            return operator;
        }

        public void setOperator(String operator) {
            this.operator = operator;
        }
        
        @XmlJavaTypeAdapter(MapAdapter.class)
        public Map<String, String> getWorkflowParams() {
            return workflowParams;
        }

        public void setWorkflowParams(Map<String, String> workflowParams) {
            this.workflowParams = workflowParams;
        }
        
    }
    /**
     * Complete task instance
     * @param engineTaskInstanceId engine task instance ID
     * @param operator operator
     * @param workflowParams Operation parameter for calculate transition if need
     */
    void completeTaskInstance(CompleteTaskInstanceDto completeTaskInstanceDto) throws ProcessException;

    /**
     * Batch complete task instances
     * @param batchDTO DTO<EngineTaskInstanceID, WorkflowParams>
     * @param opeartor Operator
     */
    void batchCompleteTaskIntances(List<CompleteTaskInstanceDto> batchDTO) throws ProcessException;

    /**
     * Obtain task instance extend attribute
     * @param engineTaskInstanceId engine task instance
     * @return extend attributes of task
     */
    HashMap<String, String> getTaskInstanceExtendAttrs(String engineTaskInstanceId);

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
