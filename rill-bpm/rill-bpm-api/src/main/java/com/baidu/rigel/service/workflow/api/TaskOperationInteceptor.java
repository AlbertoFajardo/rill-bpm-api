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

import com.baidu.rigel.service.workflow.api.exception.ProcessException;

/**
 * Task operation interceptor.
 * @author mengran
 */
public interface TaskOperationInteceptor {

    /**
     * @return Operation Type
     */
    WorkflowOperations.PROCESS_OPERATION_TYPE handleOpeationType();

    void preOperation(Object taskExecutionContext) throws ProcessException;

    void postOperation(String engineProcessInstanceId) throws ProcessException;
}
