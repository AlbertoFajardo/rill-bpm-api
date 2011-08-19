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
package com.baidu.rigel.service.workflow.api.activiti;

import java.util.HashMap;
import java.util.Map;

import org.activiti.engine.task.Task;

/**
 * Task execution context.
 * @author mengran
 */
public class ActivitiTaskExecutionContext {

    private String taskInstanceId;
    private String processInstanceId;
    private String businessObjectId;
    private Map<String, String> taskExtendAttributes;
    private Task activityContentResponse;
    private Map<String, Object> workflowParams = new HashMap<String, Object>();
    private String operator;
    private String preTaskInstanceId;
    private String taskTag;
    private String taskRoleTag;
    private boolean processFinished;
    private boolean subProcess;
    private Task currentTask;
    private Map<String, Object> otherInfos = new HashMap<String, Object>();

    public Task getCurrentTask() {
        return currentTask;
    }

    public void setCurrentTask(Task currentTask) {
        this.currentTask = currentTask;
    }

    /**
     * @return the subProcess
     */
    public boolean isSubProcess() {
        return subProcess;
    }

    /**
     * @param subProcess the subProcess to set
     */
    public void setSubProcess(boolean subProcess) {
        this.subProcess = subProcess;
    }

    /**
     * @return the processFinished
     */
    public boolean isProcessFinished() {
        return processFinished;
    }

    /**
     * @param processFinished the processFinished to set
     */
    public void setProcessFinished(boolean processFinished) {
        this.processFinished = processFinished;
    }

    /**
     * @return the taskInstanceId
     */
    public String getTaskInstanceId() {
        return taskInstanceId;
    }

    /**
     * @param taskInstanceId the taskInstanceId to set
     */
    public void setTaskInstanceId(String taskInstanceId) {
        this.taskInstanceId = taskInstanceId;
    }

    /**
     * @return the processInstanceId
     */
    public String getProcessInstanceId() {
        return processInstanceId;
    }

    /**
     * @param processInstanceId the processInstanceId to set
     */
    public void setProcessInstanceId(String processInstanceId) {
        this.processInstanceId = processInstanceId;
    }

    /**
     * @return the businessObjectId
     */
    public String getBusinessObjectId() {
        return businessObjectId;
    }

    /**
     * @param businessObjectId the businessObjectId to set
     */
    public void setBusinessObjectId(String businessObjectId) {
        this.businessObjectId = businessObjectId;
    }

    /**
     * @return the taskExtendAttributes
     */
    public final Map<String, String> getTaskExtendAttributes() {
        return taskExtendAttributes;
    }

    /**
     * @param taskExtendAttributes the taskExtendAttributes to set
     */
    public final void setTaskExtendAttributes(
            Map<String, String> taskExtendAttributes) {
        this.taskExtendAttributes = taskExtendAttributes;
    }

    /**
     * @return the activityContentResponse
     */
    public final Task getActivityContentResponse() {
        return activityContentResponse;
    }

    /**
     * @param activityContentResponse the activityContentResponse to set
     */
    public final void setActivityContentResponse(Task activityContentResponse) {
        this.activityContentResponse = activityContentResponse;
    }

    /**
     * @return the workflowParams
     */
    public Map<String, Object> getWorkflowParams() {
        return workflowParams;
    }

    /**
     * @param workflowParams the workflowParams to set
     */
    public void setWorkflowParams(Map<String, Object> workflowParams) {
        this.workflowParams = workflowParams;
    }

    /**
     * @return the operator
     */
    public String getOperator() {
        return operator;
    }

    /**
     * @param operator the operator to set
     */
    public void setOperator(String operator) {
        this.operator = operator;
    }

    /**
     * @return the preTaskInstanceId
     */
    public String getPreTaskInstanceId() {
        return preTaskInstanceId;
    }

    /**
     * @param preTaskInstanceId the preTaskInstanceId to set
     */
    public void setPreTaskInstanceId(String preTaskInstanceId) {
        this.preTaskInstanceId = preTaskInstanceId;
    }

    /**
     * @return the otherInfos
     */
    public Map<String, Object> getOtherInfos() {
        return otherInfos;
    }

    /**
     * @param otherInfos the otherInfos to set
     */
    public void setOtherInfos(Map<String, Object> otherInfos) {
        this.otherInfos = otherInfos;
    }

    /**
     * @return the taskTag
     */
    public String getTaskTag() {
        return taskTag;
    }

    /**
     * @param taskTag the taskTag to set
     */
    public void setTaskTag(String taskTag) {
        this.taskTag = taskTag;
    }

    /**
     * @return the taskRoleTag
     */
    public String getTaskRoleTag() {
        return taskRoleTag;
    }

    /**
     * @param taskRoleTag the taskRoleTag to set
     */
    public void setTaskRoleTag(String taskRoleTag) {
        this.taskRoleTag = taskRoleTag;
    }

    @Override
    public String toString() {

        StringBuilder sb = new StringBuilder();
        sb.append("Task execution context[");
        // Append variable
        sb.append("\n--taskInstanceId:").append(taskInstanceId);
        sb.append("]");

        return sb.toString();
    }
}
