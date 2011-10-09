/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.api;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import java.util.HashMap;
import java.util.Map;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

/**
 * Remote access work-flow API.
 * @author mengran
 */
public interface RemoteWorkflowOperations extends WorkflowOperations {

    // ------------------------- Operation API -------------------------------//
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
     * Create process instance API.
     * @param createProcessInstanceDto DTO for process creation.
     * @throws ProcessException Exception occurred when process creation.
     */
    void createProcessInstance(CreateProcessInstanceDto createProcessInstanceDto) throws ProcessException;

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
     * Complete task instance API.
     * @param completeTaskInstanceDto DTO for task completion.
     * @throws ProcessException Exception occurred when task completion.
     */
    void completeTaskInstance(CompleteTaskInstanceDto completeTaskInstanceDto) throws ProcessException;
    
    // ------------------------- Query API -----------------------------------//
    
    /**
     * Get engine process instance ID by BO id.
     * @param processDefinitionKey process definition KEY
     * @param boId Business Object Id
     * @return Process Instance Id in engine, NULL if not found.
     * @throws IllegalArgumentException when parameter given is empty.
     * @see org.springframework.util.StringUtils#hasText(java.lang.String) 
     */
    String getEngineProcessInstanceIdByBOId(String processDefinitionKey, String boId);
}
