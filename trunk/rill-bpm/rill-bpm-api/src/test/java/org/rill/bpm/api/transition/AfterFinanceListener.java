/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.api.transition;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.activiti.RetrieveNextTasksHelper.TransitionTakeEventListener;

/**
 *
 * @author mengran
 */
public class AfterFinanceListener extends TransitionTakeEventListener {

	protected final Log log = LogFactory.getLog(getClass().getName());
    
    @Override
    public void onTransitionTake(DelegateExecution execution, String processInstanceId, TransitionImpl transition) {
        
        // Delegate this operation
        recycle(execution);
    }
    
    private void recycle(DelegateExecution execution) {

        log.debug("In service-task change variable of execution is effect way.");
        execution.setVariable("restartFinanceProcess", -1);
        log.debug("call java service task [recycle] successfully.");
//        RuntimeService runtimeService = ProcessEngines.getDefaultProcessEngine().getRuntimeService();
//        runtimeService.setVariable(execution.getProcessInstanceId(), "restartFinanceProcess", -1);
    }
    
}
