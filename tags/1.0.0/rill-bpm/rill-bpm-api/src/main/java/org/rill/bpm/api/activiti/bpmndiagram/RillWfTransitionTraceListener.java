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
package org.rill.bpm.api.activiti.bpmndiagram;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
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
import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.activiti.RetrieveNextTasksHelper.TransitionTakeEventListener;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;
import org.springframework.util.CollectionUtils;


/**
 * Record taked transition into [RILL_WF_TRANSITION_TAKE_TRACE] table.
 * @author mengran
 */
public class RillWfTransitionTraceListener extends TransitionTakeEventListener implements BeanFactoryAware {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
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
                    pst = c.prepareStatement("insert into RILL_WF_TRANSITION_TAKE_TRACE(TRANSITION_ID_, TRANSITION_NAME_, PROC_INST_ID_) values(?,?,?)");
                    pst.setString(1, transition.getId());
                    pst.setString(2, transition.toString());
                    pst.setString(3, processInstanceId);
                    pst.execute();
                } catch (SQLException ex) {
                    logger.error("Exception occurred when try to insert transition take trace. ProcessInstanceId:" + processInstanceId + ", transition:" + transition, ex);
                } finally {
                    if (pst != null) {
                        try {
                            pst.close();
                            pst = null;
                        } catch (SQLException ex) {
                            logger.warn("Exception occurred when try to insert transition take trace. ProcessInstanceId:" + processInstanceId + ", transition:" + transition, ex);
                        }
                    }
                }
                
                return null;
            }
        }});
    }

    @SuppressWarnings("unchecked")
	public Map<String, List<String[]>> getTakedTransitions(final String processInstanceId) {

    	WorkflowOperations workflowAccessor =  beanFactory.getBean("workflowAccessor", WorkflowOperations.class);
    	final ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
    	
        // Do Search
        return (Map<String, List<String[]>>) ReflectUtil.invoke(activitiAccessor, "runExtraCommand",
                new Object[] {new Command<Map<String, List<String[]>>>(){
            
            private void deeplyFirstRetrieveTransition(CommandContext commandContext, Map<String, List<String[]>> allTransitions, String processInstance) {
            	
            	List<String[]> takedTransitions = new ArrayList<String[]>();
                Connection c = commandContext.getDbSqlSession().getSqlSession().getConnection();
                PreparedStatement pst = null;
                ResultSet rs = null;
                try {
                    pst = c.prepareStatement("select TRANSITION_ID_, TRANSITION_NAME_ from RILL_WF_TRANSITION_TAKE_TRACE where PROC_INST_ID_ = ? order by ID_ ASC");
                    pst.setString(1, processInstance);
                    rs = pst.executeQuery();
                    while (rs.next()) {
                        takedTransitions.add(new String[] {rs.getString(1), rs.getString(2)});
                    }
                } catch (SQLException ex) {
                	logger.error("Exception occurred when try to get transition take trace. ProcessInstanceId:" + processInstanceId, ex);
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
                            Logger.getLogger(RillWfTransitionTraceListener.class.getName()).log(Level.SEVERE, null, ex);
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
            
            public Map<String, List<String[]>> execute(CommandContext commandContext) {

                Map<String, List<String[]>> allTransitions = new LinkedHashMap<String, List<String[]>>();
                
                deeplyFirstRetrieveTransition(commandContext, allTransitions, processInstanceId);
                
                return allTransitions;
            }
        }});
    }

}
