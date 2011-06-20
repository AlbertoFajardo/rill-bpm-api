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
