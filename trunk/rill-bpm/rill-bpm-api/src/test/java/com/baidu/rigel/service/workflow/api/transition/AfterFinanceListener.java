/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api.transition;

import com.baidu.rigel.service.workflow.api.activiti.RetrieveNextTasksHelper.TransitionTakeEventListener;
import java.util.logging.Logger;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.process.TransitionImpl;

/**
 *
 * @author mengran
 */
public class AfterFinanceListener extends TransitionTakeEventListener {

    static final Logger log = Logger.getLogger(AfterFinanceListener.class.getName());
    
    @Override
    public void onTransitionTake(DelegateExecution execution, String processInstanceId, TransitionImpl transition) {
        
        // Delegate this operation
        recycle(execution);
    }
    
    private void recycle(DelegateExecution execution) {

        log.fine("In service-task change variable of execution is effect way.");
        execution.setVariable("restartFinanceProcess", -1);
        log.fine("call java service task [recycle] successfully.");
//        RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
//        runtimeService.setVariable(execution.getProcessInstanceId(), "restartFinanceProcess", -1);
    }
    
}
