/* Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package com.baidu.rigel.service.workflow.api.activiti;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.springframework.beans.factory.FactoryBean;

/**
 * Process configuration factory for spring v2.5
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
