/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram;

import com.baidu.rigel.service.workflow.api.activiti.ActivitiTemplate;
import com.baidu.rigel.service.workflow.api.activiti.RetrieveNextTasksHelper.TransitionTakeEventListener;
import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.ArrayList;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.pvm.process.TransitionImpl;
import org.activiti.engine.impl.util.ReflectUtil;
import org.springframework.beans.BeansException;
import org.springframework.beans.factory.BeanFactory;
import org.springframework.beans.factory.BeanFactoryAware;

/**
 * 
 * @author mengran
 */
public class RigelWfTransitionTraceListener extends TransitionTakeEventListener implements BeanFactoryAware {

    private BeanFactory beanFactory;

    public void setBeanFactory(BeanFactory beanFactory) throws BeansException {
        this.beanFactory = beanFactory;
    }

    @Override
    public void onTransitionTake(final String processInstanceId, final TransitionImpl transition) {

        // Do insert
        ReflectUtil.invoke(beanFactory.getBean("workflowAccessor", ActivitiTemplate.class), "runExtraCommand",
                new Object[] {new Command<Object>(){

            public Object execute(CommandContext commandContext) {

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

                    return null;
                }
            }
        }});
    }

    public List<String> getTakedTransitions(final String processInstanceId) {

        // Do Search
        return (List<String>) ReflectUtil.invoke(beanFactory.getBean("workflowAccessor", ActivitiTemplate.class), "runExtraCommand",
                new Object[] {new Command<List<String>>(){

            public List<String> execute(CommandContext commandContext) {

                List<String> takedTransitions = new ArrayList<String>();
                Connection c = commandContext.getDbSqlSession().getSqlSession().getConnection();
                PreparedStatement pst = null;
                ResultSet rs = null;
                try {
                    pst = c.prepareStatement("select TRANSITION_ID_ from RIGEL_WF_TRANSITION_TAKE_TRACE where PROC_INST_ID_ = ?");
                    pst.setString(1, processInstanceId);
                    rs = pst.executeQuery();
                    while (rs.next()) {
                        takedTransitions.add(rs.getString(1));
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

                    return takedTransitions;
                }
            }
        }});
    }

}
