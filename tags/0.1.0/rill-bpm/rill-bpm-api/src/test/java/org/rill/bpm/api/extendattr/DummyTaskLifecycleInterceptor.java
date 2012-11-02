/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm.api.extendattr;

import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.TaskLifecycleInteceptorAdapter;

/**
 *
 * @author mengran
 */
public class DummyTaskLifecycleInterceptor extends TaskLifecycleInteceptorAdapter {

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
