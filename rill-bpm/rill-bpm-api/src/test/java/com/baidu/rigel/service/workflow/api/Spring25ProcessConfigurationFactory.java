/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.FactoryBean;

/**
 *
 * @author mengran
 */
public class Spring25ProcessConfigurationFactory implements FactoryBean {

    private ProcessEngineConfigurationImpl processEngineConfiguration;

    private String processEngineName;

    public String getProcessEngineName() {
        return processEngineName;
    }

    public void setProcessEngineName(String processEngineName) {
        this.processEngineName = processEngineName;
    }
    
    public ProcessEngineConfigurationImpl getProcessEngineConfiguration() {
        return processEngineConfiguration;
    }

    public void setProcessEngineConfiguration(ProcessEngineConfigurationImpl processEngineConfiguration) {
        this.processEngineConfiguration = processEngineConfiguration;
    }

    public Object getObject() throws Exception {

        return getProcessEngineConfiguration().setProcessEngineName(processEngineName);
    }

    public Class getObjectType() {

        return ProcessEngineConfigurationImpl.class;
    }

    public boolean isSingleton() {
        return true;
    }



}
