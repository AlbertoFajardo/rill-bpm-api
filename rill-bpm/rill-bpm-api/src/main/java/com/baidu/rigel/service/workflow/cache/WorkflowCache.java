/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.cache;

/**
 * Work flow component cache interface.
 * <p>
 *  WorkflowCache provider for the core data of work flow component, such as task
 *  instance related informations.
 *
 *  It's can work in the cluster environment and safe concurrent access.
 * @author mengran
 */
public interface WorkflowCache {

    enum TaskInformations {

        PROCESS_INSTANCE_ID, TASK_TAG, TASK_ROLE_TAG, BUSINESS_OBJECT_ID, CLASSDELEGATE_ADAPTER_TLI, CLASSDELEGATE_ADAPTER_TOI, FORM_KEY, TASK_SERVICE_INVOKE_EXPRESSION
    }
    
    String getTaskInfos(String taskInstanceId, TaskInformations taskInformations);

    void releaseTaskInfos(String taskInstanceId);
}
