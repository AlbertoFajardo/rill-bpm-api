/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.impl.ProcessInstanceQueryImpl;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;

/**
 *
 * @author mengran
 */
public class ContinousPerformanceResultBuild implements Command<Object> {

    private long timeCost;
    private long perTaskCostTimeMax;
    private long perTaskCostTimeMin;
    private long perTaskCostTimeAvg;
    private boolean cpTablePresent = false;

    public ContinousPerformanceResultBuild(long timeCost, long perTaskCostTimeMax,
           long perTaskCostTimeMin, long perTaskCostTimeAvg) {

        this.timeCost = timeCost;
        this.perTaskCostTimeMax = perTaskCostTimeMax;
        this.perTaskCostTimeMin = perTaskCostTimeMin;
        this.perTaskCostTimeAvg = perTaskCostTimeAvg;
    }

    public Object execute(CommandContext commandContext) {

        // Check schema that result table exists or not
        if (!cpTablePresent) {
            cpTablePresent = commandContext.getDbSqlSession().isTablePresent("TB_CP");
            if (!cpTablePresent) {
                commandContext.getDbSqlSession().executeMandatorySchemaResource("create", "cp");
                cpTablePresent = true;
            }
        }

        Connection c = commandContext.getDbSqlSession().getSqlSession().getConnection();
        PreparedStatement pst = null;
        try {
            ProcessInstanceQueryImpl pq = new ProcessInstanceQueryImpl(commandContext);
            long cnt = commandContext.getRuntimeSession().findProcessInstanceCountByQueryCriteria(pq);
            pst = c.prepareStatement("insert into TB_CP(processInstanceCnt, costTime, perTaskCostTimeMax, " +
                    "perTaskCostTimeMin, perTaskCostTimeAvg) values(?,?,?,?,?)");
            pst.setLong(1, cnt);
            pst.setLong(2, timeCost);
            pst.setLong(3, perTaskCostTimeMax);
            pst.setLong(4, perTaskCostTimeMin);
            pst.setLong(5, perTaskCostTimeAvg);
            pst.execute();
        } catch (SQLException ex) {
            Logger.getLogger(ContinousPerformanceResultBuild.class.getName()).log(Level.SEVERE, null, ex);
        } finally {
            if (pst != null) {
                try {
                    pst.close();
                    pst = null;
                } catch (SQLException ex) {
                    Logger.getLogger(ContinousPerformanceResultBuild.class.getName()).log(Level.SEVERE, null, ex);
                }
            }
        }

        return null;
        
    }

}
