/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm.api.support;


import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.activiti.support.EngineDrivenTLIAdapter;
import org.rill.bpm.api.processvar.DummyReceiptInfo;
import org.springframework.util.Assert;

/**
 *
 * @author mengran
 */
public class ReceiptInfoEngineDrivenTLIAdapter extends EngineDrivenTLIAdapter<DummyReceiptInfo> {

    @Override
    protected Object doEngineDriven(DummyReceiptInfo t, TaskExecutionContext taskExecutionContext) {
        
        Assert.notNull(t);
        // Do service logic
        
        return new Object();
    }

}
