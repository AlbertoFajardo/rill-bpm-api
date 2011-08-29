/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.support;

import com.baidu.rigel.service.workflow.api.activiti.ActivitiTaskExecutionContext;
import com.baidu.rigel.service.workflow.api.activiti.support.EngineDrivenTLIAdapter;
import com.baidu.rigel.service.workflow.api.processvar.DummyReceiptInfo;
import org.springframework.util.Assert;

/**
 *
 * @author mengran
 */
public class ReceiptInfoEngineDrivenTLIAdapter extends EngineDrivenTLIAdapter<DummyReceiptInfo> {

    @Override
    protected Object doEngineDriven(DummyReceiptInfo t, ActivitiTaskExecutionContext taskExecutionContext) {
        
        Assert.notNull(t);
        // Do service logic
        
        return new Object();
    }

}
