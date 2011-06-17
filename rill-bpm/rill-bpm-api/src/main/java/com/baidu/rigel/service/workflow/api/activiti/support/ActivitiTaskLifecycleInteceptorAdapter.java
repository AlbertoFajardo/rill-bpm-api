package com.baidu.rigel.service.workflow.api.activiti.support;

import java.util.Map;


import com.baidu.rigel.service.workflow.api.TaskLifecycleInteceptor;
import com.baidu.rigel.service.workflow.api.exception.ProcessException;
import com.baidu.rigel.service.workflow.api.exception.TaskInitialException;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiTaskExecutionContext;
import java.util.logging.Logger;

public class ActivitiTaskLifecycleInteceptorAdapter implements
        TaskLifecycleInteceptor {

    /** Logger available to subclasses */
    protected final static Logger logger = Logger.getLogger(ActivitiTaskLifecycleInteceptorAdapter.class.getName());

    private void checkTaskExecutionContext(Object taskContext) {

        if (!(taskContext instanceof ActivitiTaskExecutionContext)) {
            throw new TaskInitialException("Task execution context must instanceof " + ActivitiTaskExecutionContext.class.getName());
        }
    }

    public final void init(Object taskContext) throws TaskInitialException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.fine("Execute task lifecycle interceptor#init [" + this + "].");
            doInit((ActivitiTaskExecutionContext) taskContext);
        } catch (Exception e) {
            throw new TaskInitialException("Fail to initialize task execution context:" + taskContext, e);
        }
    }

    protected void doInit(ActivitiTaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public final void postComplete(Object taskContext) throws ProcessException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.fine("Execute task lifecycle interceptor#postComplete [" + this + "].");
            doPostComplete((ActivitiTaskExecutionContext) taskContext);
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }

    protected void doPostComplete(ActivitiTaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public final Map<String, Object> preComplete(Object taskContext) throws ProcessException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.fine("Execute task lifecycle interceptor#preComplete [" + this + "].");
            doPreComplete((ActivitiTaskExecutionContext) taskContext);
            return ((ActivitiTaskExecutionContext) taskContext).getWorkflowParams();
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }

    protected void doPreComplete(ActivitiTaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public final void afterComplete(Object taskContext) throws ProcessException {

        // Check context parameter
        checkTaskExecutionContext(taskContext);

        try {
            logger.fine("Execute task lifecycle interceptor#afterComplete [" + this + "].");
            doAfterComplete((ActivitiTaskExecutionContext) taskContext);
        } catch (Exception e) {
            throw new ProcessException(e);
        }

    }

    protected void doAfterComplete(ActivitiTaskExecutionContext taskExecutionContext) {
        // Do nothing
    }

    public void onExceptionOccurred(Exception e,
            TaskLifecycleInteceptor exceptionMurderer) {

        logger.fine("Execute task lifecycle interceptor#onExceptionOccurred [" + this + "].");
        // No nothing
    }
}
