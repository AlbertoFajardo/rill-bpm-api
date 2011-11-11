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
package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.LinkedHashMap;
import java.util.List;
import java.util.Map;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.activiti.engine.delegate.DelegateExecution;
import org.activiti.engine.history.HistoricProcessInstance;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.ReflectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.CollectionUtils;

import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.activiti.RetrieveNextTasksHelper.TransitionTakeEventListener;

/**
 * Record taked transition into [RIGEL_WF_TRANSITION_TAKE_TRACE] table.
 * @author mengran
 */
public class RigelWfTransitionTraceListener extends TransitionTakeEventListener implements BeanFactoryAware {

    private BeanFactory beanFactory;

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void onTransitionTake(DelegateExecution execution, final String processInstanceId, final TransitionImpl transition) {
    	
    	WorkflowOperations workflowAccessor =  beanFactory.getBean("workflowAccessor", WorkflowOperations.class);
        // Do insert
        ReflectUtil.invoke(ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class), "runExtraCommand",
                new Object[] {new Command<Void>(){

            public Void execute(CommandContext commandContext) {

                Connection c = commandContext.getDbSqlSession().getSqlSession().getConnection();
                
                PreparedStatement pst = null;
                try {
                    pst = c.prepareStatement("insert into RIGEL_WF_TRANSITION_TAKE_TRACE(TRANSITION_ID_, TRANSITION_NAME_, PROC_INST_ID_) values(?,?,?)");
                    pst.setString(1, transition.getId());
                    pst.setString(2, transition.toString());
                    pst.setString(3, processInstanceId);
                    pst.execute();
                } catch (SQLException ex) {
                    Logger.getLogger(RigelWfTransitionTraceListener.class.getName()).log(Level.SEVERE, null, ex);
                } finally {
                    if (pst != null) {
                        try {
                            pst.close();
                            pst = null;
                        } catch (SQLException ex) {
                            Logger.getLogger(RigelWfTransitionTraceListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                }
                
                return null;
            }
        }});
    }

    @SuppressWarnings("unchecked")
	public Map<String, Map<String, String>> getTakedTransitions(final String processInstanceId) {

    	WorkflowOperations workflowAccessor =  beanFactory.getBean("workflowAccessor", WorkflowOperations.class);
    	final ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
    	
        // Do Search
        return (Map<String, Map<String, String>>) ReflectUtil.invoke(activitiAccessor, "runExtraCommand",
                new Object[] {new Command<Map<String, Map<String, String>>>(){
            
            private void deeplyFirstRetrieveTransition(CommandContext commandContext, Map<String, Map<String, String>> allTransitions, String processInstance) {
            	
            	Map<String, String> takedTransitions = new LinkedHashMap<String, String>();
                Connection c = commandContext.getDbSqlSession().getSqlSession().getConnection();
                PreparedStatement pst = null;
                ResultSet rs = null;
                try {
                    pst = c.prepareStatement("select TRANSITION_ID_, TRANSITION_NAME_ from RIGEL_WF_TRANSITION_TAKE_TRACE where PROC_INST_ID_ = ? order by ID_ ASC");
                    pst.setString(1, processInstance);
                    rs = pst.executeQuery();
                    while (rs.next()) {
                        takedTransitions.put(rs.getString(1), rs.getString(2));
                    }
                } catch (SQLException ex) {
                    Logger.getLogger(RigelWfTransitionTraceListener.class.getName()).log(Level.SEVERE, null, ex);
                } finally {

                    if (pst != null) {
                        try {
                            if (rs != null) {
                                rs.close();
                                rs = null;
                            }
                            pst.close();
                            pst = null;
                        } catch (SQLException ex) {
                            Logger.getLogger(RigelWfTransitionTraceListener.class.getName()).log(Level.SEVERE, null, ex);
                        }
                    }
                    
                    allTransitions.put(processInstance, takedTransitions);
                }
                
                List<HistoricProcessInstance> subProcesses = activitiAccessor.getHistoryService().createHistoricProcessInstanceQuery()
                		.superProcessInstanceId(processInstance).orderByProcessInstanceStartTime().asc().list();
                if (CollectionUtils.isEmpty(subProcesses)) return;
                
                for (HistoricProcessInstance hp : subProcesses) {
                	deeplyFirstRetrieveTransition(commandContext, allTransitions, hp.getId());
                }
            }
            
            public Map<String, Map<String, String>> execute(CommandContext commandContext) {

                Map<String, Map<String, String>> allTransitions = new LinkedHashMap<String, Map<String, String>>();
                
                deeplyFirstRetrieveTransition(commandContext, allTransitions, processInstanceId);
                
                return allTransitions;
            }
        }});
    }

}
