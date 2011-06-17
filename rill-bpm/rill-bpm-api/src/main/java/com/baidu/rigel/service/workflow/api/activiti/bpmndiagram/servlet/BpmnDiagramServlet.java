/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram.servlet;

import com.baidu.rigel.service.workflow.api.activiti.bpmndiagram.ProcessMonitorChartInfoHelper;
import com.baidu.rigel.service.workflow.api.activiti.bpmndiagram.ProcessMonitorChartInfoHelper.ChartInfo;
import java.io.IOException;
import java.text.DateFormat;
import java.text.SimpleDateFormat;
import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.servlet.ServletConfig;
import javax.servlet.ServletException;
import javax.servlet.http.HttpServlet;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;
import org.activiti.engine.ProcessEngine;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.history.HistoricTaskInstance;
import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.util.json.JSONWriter;
import org.activiti.engine.task.Task;
import org.apache.velocity.app.VelocityEngine;
import org.apache.velocity.exception.VelocityException;
import org.springframework.ui.velocity.VelocityEngineFactory;
import org.springframework.ui.velocity.VelocityEngineUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.WebApplicationContextUtils;

/**
 * Process monitor chart presentation tier class.
 * 
 * @author mengran
 */
public class BpmnDiagramServlet extends HttpServlet {

    private static final Logger logger = Logger.getLogger(BpmnDiagramServlet.class.getName());
    private static final DateFormat df = new SimpleDateFormat("yyyy-MM-dd hh:mm:ss");
    private ProcessMonitorChartInfoHelper processMonitorChartInfoHelper;
    private ProcessEngine processEngine;
    private VelocityEngine velocityEngine;

    private String initResourceLoaderPath = "classpath:/com/baidu/rigel/service/workflow/api/activiti/bpmndiagram/servlet/";
    private String initVelocityTemplateName = "bpmn.vm";

    @Override
    public void init(ServletConfig sc) throws ServletException {
        super.init(sc);
        
        // Init parameter inject
        if (sc.getInitParameter("initResourceLoaderPath") != null) {
            initResourceLoaderPath = sc.getInitParameter("initResourceLoaderPath").trim();
        }
        if (sc.getInitParameter("initVelocityTemplateName") != null) {
            initVelocityTemplateName = sc.getInitParameter("initVelocityTemplateName").trim();
        }

        // Velocity engine initialize
        VelocityEngineFactory factory = new VelocityEngineFactory();
        factory.setPreferFileSystemAccess(false);
        factory.setResourceLoaderPath(initResourceLoaderPath);
        try {
            velocityEngine = factory.createVelocityEngine();
        } catch (IOException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new ServletException(ex);
        } catch (VelocityException ex) {
            logger.log(Level.SEVERE, null, ex);
            throw new ServletException(ex);
        }
    }

    @Override
    protected void doGet(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        handleRequest(req, resp);
    }


    protected void handleRequest(HttpServletRequest request, HttpServletResponse response) {

        try {
            synchronized (this) {
                if (processMonitorChartInfoHelper == null) {
                    WebApplicationContext wac = WebApplicationContextUtils.getRequiredWebApplicationContext(getServletContext());
                    processMonitorChartInfoHelper = (ProcessMonitorChartInfoHelper) wac.getBean("processMonitorChartInfoHelper");
                    processEngine = ProcessEngines.getProcessEngine(((ProcessEngineConfigurationImpl) wac.getBean("processEngineConfiguration")).getProcessEngineName());
                    if (processMonitorChartInfoHelper == null) {
                        response.sendError(HttpServletResponse.SC_BAD_REQUEST, "NO PROCESS MONITOR CHART INFO HELPER BEAN!");
                        return;
                    }
                }
            }
            logger.info("Handle request:" + request.getPathInfo());
            if (request.getPathInfo() == null || "/".equals(request.getPathInfo())) {
                doGetBpmnDiagram(request, response);
            } else if ("/diagram".equals(request.getPathInfo())) {
                doGetDiagram(request, response);
            } else if ("/taskinfo".equals(request.getPathInfo())) {
                doGetTaskinfo(request, response);
            } else {
                response.sendError(HttpServletResponse.SC_BAD_REQUEST, "Unsupported request:" + request.getServletPath());
            }
        } catch (Exception e) {
            try {
                response.sendError(HttpServletResponse.SC_BAD_GATEWAY, e.getMessage());
            } catch (IOException ex) {
                Logger.getLogger(BpmnDiagramServlet.class.getName()).log(Level.SEVERE, null, ex);
            }
        }
    }

    private void doGetBpmnDiagram(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String processInstanceId = req.getParameter("processInstanceId");
        if (processInstanceId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "NO PROCESS INSTANCE ID!");
            return;
        }

        ChartInfo chartInfo = processMonitorChartInfoHelper.getMonitorChartInfo(processInstanceId);
        if (chartInfo == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "PROCESS INSTANCE IS ENDED or PROCESS INSTANCE ID IS INVALID!");
            return;
        }

        Map<String, Object> cxt = new HashMap<String, Object>();
        Map<String, List<Integer>> mapArea = new HashMap<String, List<Integer>>();
        for (Entry<String, List<Integer>> entry : chartInfo.getTaskDefinitionKeyPosition().entrySet()) {
            List<Integer> tempList = new ArrayList<Integer>(4);
            tempList.add(entry.getValue().get(0));
            tempList.add(entry.getValue().get(1));
            tempList.add(entry.getValue().get(0) + entry.getValue().get(2));
            tempList.add(entry.getValue().get(1) + entry.getValue().get(3));
            mapArea.put(entry.getKey(), tempList);
        }
        cxt.put("taskDefinitionPositionMap", mapArea);
        cxt.put("contextPath", req.getContextPath());
        cxt.put("processInstanceId", processInstanceId);
        VelocityEngineUtils.mergeTemplate(velocityEngine, initVelocityTemplateName, cxt, resp.getWriter());
        return;
    }
    
    protected void doGetDiagram(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String processInstanceId = req.getParameter("processInstanceId");
        if (processInstanceId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "NO PROCESS INSTANCE ID!");
            return;
        }

        ChartInfo chartInfo = processMonitorChartInfoHelper.getMonitorChartInfo(processInstanceId);
        if (chartInfo == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "PROCESS INSTANCE IS ENDED or PROCESS INSTANCE ID IS INVALID!");
            return;
        }
        resp.setContentType("image/png");
        resp.setHeader("Pragma", "no-cache");
        resp.setHeader("Cache-Control", "no-cache");
        resp.setDateHeader("Expires", 0);
        resp.setContentLength(chartInfo.getDiagramBytes().length);
        resp.getOutputStream().write(chartInfo.getDiagramBytes());
        resp.getOutputStream().flush();
    }

    protected void doGetTaskinfo(HttpServletRequest req, HttpServletResponse resp) throws ServletException, IOException {

        String processInstanceId = req.getParameter("processInstanceId");
        if (processInstanceId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "NO PROCESS INSTANCE ID!");
            return;
        }

        String activityDefineId = req.getParameter("activityDefineId");
        if (activityDefineId == null) {
            resp.sendError(HttpServletResponse.SC_BAD_REQUEST, "NO ACTIVITY DEFINE ID!");
            return;
        }

        List<Task> taskList = processEngine.getTaskService().createTaskQuery().processInstanceId(processInstanceId)
                .taskDefinitionKey(activityDefineId).list();
        List<HistoricTaskInstance> historicTaskList = null;
        if (taskList == null || taskList.isEmpty()) {
            historicTaskList = processEngine.getHistoryService().createHistoricTaskInstanceQuery().processInstanceId(processInstanceId)
                    .taskDefinitionKey(activityDefineId).list();
            if (historicTaskList == null || historicTaskList.isEmpty()) {
                // Can not find task informations and throws error.
                resp.setContentType("application/json");
                JSONWriter jsonWriter = new JSONWriter(resp.getWriter());
                jsonWriter.object()
                        .endObject();
                resp.getWriter().flush();
                return;
            } else {
                // Return historic task information
                resp.setContentType("application/json");
                JSONWriter jsonWriter = new JSONWriter(resp.getWriter());
                jsonWriter.object()
                        .key("taskExeMan").value(historicTaskList.get(0).getAssignee())
                        .key("taskCompleteTime").value(historicTaskList.get(0).getEndTime() == null ? "" : df.format(historicTaskList.get(0).getEndTime()))
                        .key("taskInitialTime").value(historicTaskList.get(0).getStartTime() == null ? "" : df.format(historicTaskList.get(0).getStartTime()))
                        .key("activitiDefineId").value(activityDefineId)
                        .endObject();
                resp.getWriter().flush();
            }
        } else {
            // Return active task information
            resp.setContentType("application/json");
            JSONWriter jsonWriter = new JSONWriter(resp.getWriter());
            jsonWriter.object()
                    .key("taskExeMan").value(taskList.get(0).getAssignee())
                    .key("taskCompleteTime").value("Running...")
                    .key("taskInitialTime").value(taskList.get(0).getCreateTime() == null ? "" : df.format(taskList.get(0).getCreateTime()))
                    .key("activitiDefineId").value(activityDefineId)
                    .endObject();
            resp.getWriter().flush();
        }
        
    }
    
}
