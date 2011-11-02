/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.extendattr;

import com.baidu.rigel.service.workflow.api.TaskExecutionContext;
import com.baidu.rigel.service.workflow.api.activiti.support.ActivitiTaskLifecycleInteceptorAdapter;

/**
 *
 * @author mengran
 */
public class DummyTaskLifecycleInterceptor extends ActivitiTaskLifecycleInteceptorAdapter {

    @Override
    protected void doAfterComplete(TaskExecutionContext taskExecutionContext) {

        StatefulDummyTLIStatusHolder.holdTLIMethodCallStatus(taskExecutionContext.getTaskInstanceId(),
                StatefulDummyTLIStatusHolder.TLI_METHOD.after);
    }

    @Override
    protected void doInit(TaskExecutionContext taskExecutionContext) {

        StatefulDummyTLIStatusHolder.holdTLIMethodCallStatus(taskExecutionContext.getTaskInstanceId(),
                StatefulDummyTLIStatusHolder.TLI_METHOD.init);
    }

    @Override
    protected void doPostComplete(TaskExecutionContext taskExecutionContext) {

        StatefulDummyTLIStatusHolder.holdTLIMethodCallStatus(taskExecutionContext.getTaskInstanceId(),
                StatefulDummyTLIStatusHolder.TLI_METHOD.post);
    }

    @Override
    protected void doPreComplete(TaskExecutionContext taskExecutionContext) {

        StatefulDummyTLIStatusHolder.holdTLIMethodCallStatus(taskExecutionContext.getTaskInstanceId(),
                StatefulDummyTLIStatusHolder.TLI_METHOD.pre);
    }


}
