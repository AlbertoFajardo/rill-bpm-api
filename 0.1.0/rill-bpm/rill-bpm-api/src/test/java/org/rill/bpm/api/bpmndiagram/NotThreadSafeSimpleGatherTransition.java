/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm.api.bpmndiagram;

import java.util.ArrayList;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.rill.bpm.api.activiti.RetrieveNextTasksHelper.TransitionTakeEventListener;

/**
 *
 * @author mengran
 */
public class NotThreadSafeSimpleGatherTransition extends TransitionTakeEventListener {

    public static final Map<String, List<String>> gatherInfo = new LinkedHashMap<String, List<String>>();

    @Override
    public void onTransitionTake(DelegateExecution execution, String processInstanceId, TransitionImpl transition) {

        if (!gatherInfo.containsKey(processInstanceId)) {
            gatherInfo.put(processInstanceId, new ArrayList<String>());
        }
        List<String> takeInfo = gatherInfo.get(processInstanceId);
        takeInfo.add(transition.getId());
    }
}
