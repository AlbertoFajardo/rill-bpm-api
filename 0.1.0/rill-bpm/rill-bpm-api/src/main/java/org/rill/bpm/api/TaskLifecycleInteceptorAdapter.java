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
package org.rill.bpm.api;

import java.util.HashMap;
import java.util.Map;
import java.util.concurrent.atomic.AtomicReference;

import javax.annotation.Resource;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.exception.ProcessException;
import org.rill.bpm.api.exception.TaskInitialException;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

public class TaskLifecycleInteceptorAdapter implements
        TaskLifecycleInteceptor, BeanFactoryAware {

    /** Logger available to subclasses */
	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	protected AtomicReference<WorkflowOperations> workflowAccessor = new AtomicReference<WorkflowOperations>();
	private BeanFactory heldBeanFactory;
	@Resource(name="workflowCache")
	private WorkflowCache<HashMap<String, String>> workflowCache;
	
	public final WorkflowCache<HashMap<String, String>> getWorkflowCache() {
		return workflowCache;
	}

	public final void setWorkflowCache(WorkflowCache<HashMap<String, String>> workflowCache) {
		this.workflowCache = workflowCache;
	}

	public final WorkflowOperations getWorkflowAccessor() {
		return workflowAccessor.get();
	}
	
	// Use by jUnit
	public final void setWorkflowOperations(WorkflowOperations workflowOperations) {
		this.workflowAccessor.compareAndSet(null, workflowOperations);
	}

	@Override
	public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
		heldBeanFactory = beanFactory;
	}

    private void checkTaskExecutionContext(Object taskContext) {

        if (!(taskContext instanceof TaskExecutionContext)) {
            throw new TaskInitialException("Task execution context must instanceof " + TaskExecutionContext.class.getName());
        }
        
        // bean factory maybe null when jUnit
        if (workflowAccessor.get() == null) {
        	workflowAccessor.compareAndSet(null, heldBeanFactory.getBean("workflowAccessor", WorkflowOperations.class));
        }
    }

    public final void init(Object taskContext) throws TaskInitialException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.debug("Execute task lifecycle interceptor#init " + this.getClass().getName() + ".");
            doInit((TaskExecutionContext) taskContext);
        } catch (Exception e) {
            throw new TaskInitialException("Fail to initialize task execution context:" + taskContext, e);
        }
    }

    protected void doInit(TaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public final void postComplete(Object taskContext) throws ProcessException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.debug("Execute task lifecycle interceptor#postComplete " + this.getClass().getName() + ".");
            doPostComplete((TaskExecutionContext) taskContext);
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }

    protected void doPostComplete(TaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public final Map<String, Object> preComplete(Object taskContext) throws ProcessException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.debug("Execute task lifecycle interceptor#preComplete " + this.getClass().getName() + ".");
            doPreComplete((TaskExecutionContext) taskContext);
            return ((TaskExecutionContext) taskContext).getWorkflowParams();
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }

    protected void doPreComplete(TaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public final void afterComplete(Object taskContext) throws ProcessException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.debug("Execute task lifecycle interceptor#afterComplete" + this.getClass().getName() + ".");
            doAfterComplete((TaskExecutionContext) taskContext);
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }

    protected void doAfterComplete(TaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public void onExceptionOccurred(Exception e,
            TaskLifecycleInteceptor exceptionMurderer) {

        logger.debug("Execute task lifecycle interceptor#onExceptionOccurred " + this.getClass().getName() + ".");
        // No nothing
    }
}
