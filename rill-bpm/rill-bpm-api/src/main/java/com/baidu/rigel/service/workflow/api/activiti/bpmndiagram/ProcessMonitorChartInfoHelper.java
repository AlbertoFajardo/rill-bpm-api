/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram;

import com.baidu.rigel.service.workflow.api.activiti.ActivitiTemplate;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.impl.cmd.GetDeploymentProcessDefinitionCmd;
import org.activiti.engine.impl.pvm.ReadOnlyProcessDefinition;
import org.activiti.engine.impl.repository.ProcessDefinitionEntity;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;

/**
 * Process monitor chart generation helper classes.
 *
 * <p>
 *  Use {@link RigelWfTransitionTraceListener} as taked-transition info provider as default.
 * 
 * @author mengran
 */
public class ProcessMonitorChartInfoHelper {

    private ActivitiTemplate workflowAccessor;
    private RigelWfTransitionTraceListener rigelWfTransitionTraceListener;

    public RigelWfTransitionTraceListener getRigelWfTransitionTraceListener() {
        return rigelWfTransitionTraceListener;
    }

    public void setRigelWfTransitionTraceListener(RigelWfTransitionTraceListener rigelWfTransitionTraceListener) {
        this.rigelWfTransitionTraceListener = rigelWfTransitionTraceListener;
    }

    public ActivitiTemplate getWorkflowAccessor() {
        return workflowAccessor;
    }

    public void setWorkflowAccessor(ActivitiTemplate workflowAccessor) {
        this.workflowAccessor = workflowAccessor;
    }

    public static class ChartInfo {

        private byte[] diagramBytes;
        private Map<String, List<Integer>> taskDefinitionKeyPosition = new HashMap<String, List<Integer>>();

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
     * @return process monitor chart info. NULL if process is end/ process intance id is invalid.
     */
    public ChartInfo getMonitorChartInfo(String processInstanceId) {

        List<String> takedTransitions = getRigelWfTransitionTraceListener().getTakedTransitions(processInstanceId);
        // Delegate this operation
        return getMonitorChartInfo(processInstanceId, takedTransitions);
    }

    public ChartInfo getMonitorChartInfo(String processInstanceId, List<String> takedTransitions) {

        // Process is end?
        ProcessInstance processInstance = workflowAccessor.getRuntimeService().createProcessInstanceQuery().processInstanceId(processInstanceId).singleResult();
        if (processInstance == null) return null;

        ReadOnlyProcessDefinition processDefinition = (ReadOnlyProcessDefinition) ReflectUtil.invoke(workflowAccessor, "runExtraCommand",
                new Object[] {new GetDeploymentProcessDefinitionCmd(processInstance.getProcessDefinitionId())});
        Assert.notNull(processDefinition, "Can not found process definition[" + processInstance.getProcessInstanceId() + "]");

        List<String> taskDefinitionKeyList = new ArrayList<String>();
        List<Task> taskList = workflowAccessor.getTaskService().createTaskQuery().processInstanceId(processInstanceId).list();
        if (!CollectionUtils.isEmpty(taskList)) {
            for (Task task : taskList) {
                taskDefinitionKeyList.add(task.getTaskDefinitionKey());
            }
        }
        ChartInfo chartInfo = SmartSmoothDrawingPDG.generateDiagram((ProcessDefinitionEntity) processDefinition, "png",
                taskDefinitionKeyList, takedTransitions);
        
        return chartInfo;
    }
}
