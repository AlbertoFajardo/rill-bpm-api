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
package org.rill.bpm.api.activiti.support;

import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.TaskLifecycleInteceptorAdapter;
import org.rill.bpm.api.WorkflowOperations;
import org.springframework.util.Assert;

/**
 * Engine-driven design helper class.
 * @author mengran
 */
public abstract class EngineDrivenTLIAdapter<T> extends TaskLifecycleInteceptorAdapter {

    @Override
    protected final void doPreComplete(TaskExecutionContext taskExecutionContext) {

        T t = obtainTaskFormData(taskExecutionContext);
        Object returnObject = doEngineDriven(t, taskExecutionContext);
        if (returnObject != null) {
            taskExecutionContext.getOtherInfos().put(WorkflowOperations.ENGINE_DRIVEN_TASK_RETURN_DATA_KEY, returnObject);
        }
    }

    @SuppressWarnings("unchecked")
	protected final T obtainTaskFormData(TaskExecutionContext taskExecutionContext) {

        Assert.notNull(taskExecutionContext);

        T t = (T) taskExecutionContext.getOtherInfos().get(WorkflowOperations.ENGINE_DRIVEN_TASK_FORM_DATA_KEY);

        return t;
    }

    protected abstract Object doEngineDriven(T t, TaskExecutionContext taskExecutionContext);

}
