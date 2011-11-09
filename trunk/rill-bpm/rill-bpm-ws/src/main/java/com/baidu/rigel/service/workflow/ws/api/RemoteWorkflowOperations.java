/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.api;

import java.util.HashMap;
import java.util.List;
import java.util.Map;

import javax.xml.bind.annotation.XmlAccessType;
import javax.xml.bind.annotation.XmlAccessorType;
import javax.xml.bind.annotation.XmlElement;
import javax.xml.bind.annotation.XmlRootElement;
import javax.xml.bind.annotation.adapters.XmlAdapter;
import javax.xml.bind.annotation.adapters.XmlJavaTypeAdapter;

import org.springframework.util.Assert;

import com.baidu.rigel.service.workflow.api.WorkflowTemplate.WorkflowResponse;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 * Remote access work-flow API.
 * @author mengran
 */
public interface RemoteWorkflowOperations {

    // ------------------------- Operation API -------------------------------//
	public class MapElements {

        @XmlElement
        public String key;
        @XmlElement
        public String value;

        MapElements() {
        } //Required by JAXB

        public MapElements(String key, String value) {
            this.key = key;
            this.value = value;
        }
    }
    
    @XmlAccessorType(XmlAccessType.FIELD)
    public class MapAdapter extends XmlAdapter<MapElements[], Map<String, String>> {

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
    public class RemoteWorkflowResponse extends WorkflowResponse {
    	
    	boolean processInstanceEnd;
    	boolean hasParentProcessInstance;
    	
    	boolean robustReturn;
    	
    	RemoteWorkflowResponse() {
    	} //Required by JAXB

		public RemoteWorkflowResponse(WorkflowResponse workflowResponse, boolean processInstanceEnd) {
			
			this(workflowResponse.getEngineProcessInstanceId(), workflowResponse.getBusinessObjectId(), 
					workflowResponse.getProcessDefinitionKey(), workflowResponse.getEngineTaskInstanceIds(), 
					workflowResponse.getRootEngineProcessInstanceId(), processInstanceEnd);
		}
		
		public RemoteWorkflowResponse(String engineProcessInstanceId,
				String businessObjectId, String processDefinitionKey,
				List<String> engineTaskInstanceIds, String rootEngineProcessInstanceId, boolean processInstanceEnd) {
			
			super();
			Assert.notNull(rootEngineProcessInstanceId);
			
			this.setEngineProcessInstanceId(engineProcessInstanceId);
			this.setEngineTaskInstanceIds(engineTaskInstanceIds);
			this.setBusinessObjectId(businessObjectId);
			this.setProcessDefinitionKey(processDefinitionKey);
			this.setRootEngineProcessInstanceId(rootEngineProcessInstanceId);
			this.setHasParentProcessInstance(!getEngineProcessInstanceId().equals(this.getRootEngineProcessInstanceId()));
			
			this.processInstanceEnd = processInstanceEnd;
		}

		public final boolean isHasParentProcessInstance() {
			return hasParentProcessInstance;
		}

		public final void setHasParentProcessInstance(boolean hasParentProcessInstance) {
			this.hasParentProcessInstance = hasParentProcessInstance;
		}

		public final boolean isProcessInstanceEnd() {
			return processInstanceEnd;
		}

		public final void setProcessInstanceEnd(boolean processInstanceEnd) {
			this.processInstanceEnd = processInstanceEnd;
		}

		public final boolean isRobustReturn() {
			return robustReturn;
		}

		// Adapt chain-coding style
		public final RemoteWorkflowResponse setRobustReturn(boolean robustReturn) {
			this.robustReturn = robustReturn;
			return this;
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
    RemoteWorkflowResponse createProcessInstance(CreateProcessInstanceDto createProcessInstanceDto) throws ProcessException;

    /**
     * Delete process instance
     * @param engineProcessInstanceId engine process instance ID
     * @param reason delete reason
     */
    void deleteProcessInstance(String engineProcessInstanceId, String reason) throws ProcessException;
    
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
    RemoteWorkflowResponse completeTaskInstance(CompleteTaskInstanceDto completeTaskInstanceDto) throws ProcessException;
    
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
    
    /**
     * Get task extend attributes from engine.
     * @param engineTaskInstanceId engine task instance id
     * @return task extend attributes
     */
    List<String[]> getTaskInstanceExtendAttrs(String engineTaskInstanceId);
    
    /**
     * Obtain root process instance ID
     * @param processInstanceId engine process instance id
     * @return return self if process instance given is root
     */
    String getRootProcessInstanceId(String engineProcessInstanceId) throws ProcessException;
    
    /**
     * Get process insance's variable names
     * @param engineProcessInstanceId engine process instance id
     * @return process insance's variable names
     */
    String[] getProcessInstanceVariableNames(String engineProcessInstanceId);
    
    /**
     * Get process instance's variables
     * @param engineProcessInstanceId process instance ID(NOT NULL)
     * @return process instance's variables
     */
    String[] getLastedVersionProcessDefinitionVariableNames(String processDefinitionKey);
    
}
