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
package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Logger;

import javax.annotation.Resource;

import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.persistence.entity.ProcessDefinitionEntity;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.task.Task;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;

/**
 * Process monitor chart generation helper classes.
 *
 * <p>
 *  Use {@link RigelWfTransitionTraceListener} as taked-transition info provider as default.
 * 
 * @author mengran
 */
public class ProcessMonitorChartInfoHelper {

	private static final Logger logger = Logger.getLogger(ProcessMonitorChartInfoHelper.class.getName());
	
	@Resource
    private WorkflowOperations workflowAccessor;
	@Resource
    private RigelWfTransitionTraceListener rigelWfTransitionTraceListener;
	
    private ActivitiAccessor activitiAccessor;

    public RigelWfTransitionTraceListener getRigelWfTransitionTraceListener() {
        return rigelWfTransitionTraceListener;
    }

    public void setRigelWfTransitionTraceListener(RigelWfTransitionTraceListener rigelWfTransitionTraceListener) {
        this.rigelWfTransitionTraceListener = rigelWfTransitionTraceListener;
    }

    public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor;
	}

	public final void setWorkflowAccessor(WorkflowOperations workflowAccessor) {
		this.workflowAccessor = workflowAccessor;
		
		activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
	}


	public static class ChartInfo {

        private byte[] diagramBytes;
        private Map<String, List<Integer>> taskDefinitionKeyPosition = new HashMap<String, List<Integer>>();
        private Map<String, String> taskDefinitionKeyType = new HashMap<String, String>();

        public final Map<String, String> getTaskDefinitionKeyType() {
			return taskDefinitionKeyType;
		}

		public final ChartInfo setTaskDefinitionKeyType(
				Map<String, String> taskDefinitionKeyType) {
			this.taskDefinitionKeyType = taskDefinitionKeyType;
			return this;
		}

		public byte[] getDiagramBytes() {
            return diagramBytes;
        }

        public ChartInfo setDiagramBytes(byte[] diagramBytes) {
            this.diagramBytes = diagramBytes;
            return this;
        }

        public Map<String, List<Integer>> getTaskDefinitionKeyPosition() {
            return taskDefinitionKeyPosition;
        }

        public ChartInfo setTaskDefinitionKeyPosition(Map<String, List<Integer>> taskDefinitionKeyPosition) {
            this.taskDefinitionKeyPosition = taskDefinitionKeyPosition;
            return this;
        }
        
    }


    // -------------------------------- Helper method -------------------------------- //
    /**
     * Get process monitor chart info
     * @param processInstanceId process instance id
     * @return process monitor chart info. NULL if process is end/ process instance id is invalid.
     */
    public Map<String, ChartInfo> getMonitorChartInfo(String processInstanceId) {

        Map<String, Map<String, String>> takedTransitions = getRigelWfTransitionTraceListener().getTakedTransitions(processInstanceId);
        // Delegate this operation
        return getMonitorChartInfo(processInstanceId, takedTransitions);
    }

    public Map<String, ChartInfo> getMonitorChartInfo(String processInstanceId, Map<String, Map<String, String>> takedTransitions) {
    	
    	Map<String, ChartInfo> allChartInfo = new HashMap<String, ProcessMonitorChartInfoHelper.ChartInfo>();
    	
    	for (Entry<String, Map<String, String>> entry : takedTransitions.entrySet()) {
    		
    		// Process is not exists.
            HistoricProcessInstance processInstance = activitiAccessor.getHistoryService().createHistoricProcessInstanceQuery().processInstanceId(entry.getKey()).singleResult();
            if (processInstance == null) {
            	logger.warning("Can not get process instance by given id " + entry.getKey());
            	return null;
            }

            ReadOnlyProcessDefinition processDefinition = (ReadOnlyProcessDefinition) ReflectUtil.invoke(activitiAccessor, "runExtraCommand",
                    new Object[] {new GetDeploymentProcessDefinitionCmd(processInstance.getProcessDefinitionId())});
            Assert.notNull(processDefinition, "Can not found process definition[" + processInstance.getId() + "]");

            List<String> taskDefinitionKeyList = new ArrayList<String>();
            List<Task> taskList = activitiAccessor.getTaskService().createTaskQuery().processInstanceId(entry.getKey()).list();
            if (!CollectionUtils.isEmpty(taskList)) {
                for (Task task : taskList) {
                    taskDefinitionKeyList.add(task.getTaskDefinitionKey());
                }
            }
            ChartInfo chartInfo = SmartSmoothDrawingPDG.generateDiagram((ProcessDefinitionEntity) processDefinition, "png",
                    taskDefinitionKeyList, entry.getValue());
            
            allChartInfo.put(entry.getKey(), chartInfo);
    	}
        
        return allChartInfo;
    }
    
}
