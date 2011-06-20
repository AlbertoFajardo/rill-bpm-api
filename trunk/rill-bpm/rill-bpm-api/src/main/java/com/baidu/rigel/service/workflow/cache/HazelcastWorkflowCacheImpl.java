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
package com.baidu.rigel.service.workflow.cache;

import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.activiti.TLITOIClassDelegateAdapter;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import com.hazelcast.core.Hazelcast;
import java.util.ArrayList;
import java.util.List;
import java.util.concurrent.ConcurrentMap;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.FormService;
import org.activiti.engine.RuntimeService;
import org.activiti.engine.TaskService;
import org.activiti.engine.delegate.TaskListener;
import org.activiti.engine.form.TaskFormData;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.task.TaskDefinition;
import org.activiti.engine.impl.task.TaskEntity;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.springframework.util.Assert;
import org.springframework.util.ObjectUtils;

/**
 * Hazelcast(In Memory Data Grid) workflow cache implementation.
 *
 * @author mengran
 */
public class HazelcastWorkflowCacheImpl implements WorkflowCache {

    private static final Logger logger = Logger.getLogger(HazelcastWorkflowCacheImpl.class.getName());

    private ActivitiAccessor workflowAccessor;

    public ActivitiAccessor getWorkflowAccessor() {
        return workflowAccessor;
    }

    public void setWorkflowAccessor(ActivitiAccessor workflowAccessor) {
        this.workflowAccessor = workflowAccessor;
    }

    // Getter ------------------------------------------------------------------
    TaskService getTaskService() {
        return getWorkflowAccessor().getTaskService();
    }

    RuntimeService getRuntimeService() {
        return getWorkflowAccessor().getRuntimeService();
    }

    FormService getFormService() {
        return getWorkflowAccessor().getFormService();
    }

    // Implementation ----------------------------------------------------------
    public String getTaskInfos(String taskInstanceId, TaskInformations taskInformations) {
        
        Assert.hasText(taskInstanceId, "taskInstanceId pass in must not empty");
        Assert.notNull(taskInformations, "taskInformations must not null");
    
        // Delegate this operation
        return obtainFromHazelcastCache(taskInstanceId, taskInformations);
    }

    public void releaseTaskInfos(String taskInstanceId) {
        throw new UnsupportedOperationException("Not supported yet.");
    }

    /**
     * Obtain cache informations from cache. Cache data will fill in every node in cluster.
     *
     * @param taskInstanceId task instance's ID
     * @return cache information specify by enum given
     */
    private String obtainFromHazelcastCache(String taskInstanceId, TaskInformations cacheInfo) {

        ConcurrentMap<String, String[]> taskInstanceInfoCache = Hazelcast.getMap("TaskInformations");

        // Using concurrent package class to prevent thread-safe
        if (taskInstanceInfoCache.get(taskInstanceId) != null) {
            String cacheHit = taskInstanceInfoCache.get(taskInstanceId)[cacheInfo.ordinal()];
            logger.log(Level.FINE, "Hit cache of task instance id[{0}], return {1} as {2}", new Object[]{taskInstanceId, cacheHit, cacheInfo.name()});
            return cacheHit;
        }

        Task task = null;
        try {
            task = getTaskService().createTaskQuery().taskId(taskInstanceId).singleResult();
        } catch (ActivitiException e) {
            throw new ProcessException("Can't get task instance by giving ID" + taskInstanceId, e);
        }

        String[] taskRelatedInfo = new String[TaskInformations.values().length];
        taskRelatedInfo[0] = task.getProcessInstanceId();
        String activityDefineId = null;
        activityDefineId = task.getTaskDefinitionKey();
        taskRelatedInfo[1] = activityDefineId;

        final List<String> tdDefines = new ArrayList<String>(4);
        ProcessInstance pi = getRuntimeService().createProcessInstanceQuery().processInstanceId(task.getProcessInstanceId()).singleResult();
        final TaskEntity taskEntity = (TaskEntity) task;
        workflowAccessor.runExtraCommand(new Command<List<String>>() {

            public List<String> execute(CommandContext commandContext) {
                TaskDefinition td = taskEntity.getTaskDefinition();
                // FIXME: Support single role definition temporarily
                tdDefines.add(td.getCandidateGroupIdExpressions().iterator().next().getExpressionText());
                if (td.getTaskListeners() != null && td.getTaskListeners().size() > 0) {
                    for (List<TaskListener> value : td.getTaskListeners().values()) {
                        if (value != null && !value.isEmpty()) {
                            for (TaskListener tl : value) {
                                if (TLITOIClassDelegateAdapter.class.isInstance(tl)) {
                                    tdDefines.add(((TLITOIClassDelegateAdapter) tl).obtainTaskLifycycleInterceptors());
                                    tdDefines.add(((TLITOIClassDelegateAdapter) tl).obtainTaskOperationInterceptors());
                                    tdDefines.add(((TLITOIClassDelegateAdapter) tl).getTaskServiceInvokeExpression());
                                }
                            }
                        }
                    }
                }


                return tdDefines;
            }
        });

        taskRelatedInfo[2] = tdDefines.get(0);
        taskRelatedInfo[3] = pi.getBusinessKey();

        // Task extend-attributes
        taskRelatedInfo[4] = tdDefines.size() > 1 ? tdDefines.get(1) : null;
        taskRelatedInfo[5] = tdDefines.size() > 2 ? tdDefines.get(2) : null;

        TaskFormData formData = getFormService().getTaskFormData(taskInstanceId);
        taskRelatedInfo[6] = formData.getFormKey();

        // Task service invoke expression
        taskRelatedInfo[7] = tdDefines.size() > 3 ? tdDefines.get(3) : null;

        // Put into cache
        taskInstanceInfoCache.putIfAbsent(taskInstanceId, taskRelatedInfo);
        logger.log(Level.FINE, "Cache informations of task instance id[{0}].{1}", new Object[]{taskInstanceId, ObjectUtils.getDisplayString(taskRelatedInfo)});

        return taskInstanceInfoCache.get(taskInstanceId)[cacheInfo.ordinal()];
    }

}
