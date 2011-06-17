/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.support;

import com.baidu.rigel.service.workflow.api.activiti.ActivitiTaskExecutionContext;
import com.baidu.rigel.service.workflow.api.activiti.support.EngineDrivenTLIAdapter;
import com.baidu.rigel.service.workflow.api.processvar.ReceiptInfo;
import org.springframework.util.Assert;

/**
 *
 * @author mengran
 */
public class ReceiptInfoEngineDrivenTLIAdapter extends EngineDrivenTLIAdapter<ReceiptInfo> {

    @Override
    protected Object doEngineDriven(ReceiptInfo t, ActivitiTaskExecutionContext taskExecutionContext) {
        
        Assert.notNull(t);
        // Do service logic
        
        return new Object();
    }

}
