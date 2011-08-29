/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow.api.bpmndiagram;

import com.baidu.rigel.service.workflow.api.ContinuousPerformanceTests;
import com.baidu.rigel.service.workflow.api.WorkflowOperations;
import com.baidu.rigel.service.workflow.api.activiti.bpmndiagram.ProcessMonitorChartInfoHelper;
import java.io.File;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Enumeration;
import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Random;
import java.util.Set;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageOutputStream;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.test.PluggableActivitiTestCase;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.Task;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 *
 * @author mengran
 */
public class Draw2DSimpleVariableDiagramTest extends PluggableActivitiTestCase {
    
    public Draw2DSimpleVariableDiagramTest() {
    }

        /* (non-Javadoc)
     * @see org.activiti.engine.impl.test.PluggableActivitiTestCase#initializeProcessEngine()
     */
    @Override
    protected void initializeProcessEngine() {
        if (processEngine == null) {
            if (ProcessEngines.getProcessEngineInfo("simplevariableEngine") != null) {
                processEngine = ProcessEngines.getProcessEngine("simplevariableEngine");
                if (processEngine == null) {
                    throw new ActivitiException("Can not obtain registed process engine: simplevariableEngine");
                }
                return;
            }

            ClassLoader classLoader = ReflectUtil.getClassLoader();
            Enumeration<URL> resources = null;
            try {
                resources = classLoader.getResources("com/baidu/rigel/service/workflow/api/processvar/simplevariable.activiti.cfg.xml");
            } catch (IOException e) {
                throw new ActivitiException("problem retrieving activiti.cfg.xml resources on the classpath: " + System.getProperty("java.class.path"), e);
            }

            ProcessEngines.retry(resources.nextElement().toString());
            try {
                Field field = ProcessEngines.class.getDeclaredField("isInitialized");
                field.setAccessible(true);
                try {
                    field.setBoolean(ProcessEngines.class, true);
                } catch (IllegalArgumentException ex) {
                    Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
                } catch (IllegalAccessException ex) {
                    Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
                }
            } catch (NoSuchFieldException ex) {
                Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
            } catch (SecurityException ex) {
                Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
            }
            processEngine = ProcessEngines.getProcessEngine("simplevariableEngine");
            if (processEngine == null) {
                throw new ActivitiException("no default process engine available");
            }
        }
    }

    @Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support-simplevariable.bpmn20.xml"})
    public void testDrawTrace() {

        final WorkflowOperations workflowAccessor = (WorkflowOperations) processEngineConfiguration.getBeans().get("workflowAccessor");

        Integer orderId = new Random().nextInt();
        String processDefinitionKey = "pg-support-simplevariable";

        // Start process by KEY
        WorkflowOperations.CreateProcessInstanceDto createProcessInstanceDto = new WorkflowOperations.CreateProcessInstanceDto(processDefinitionKey, "Rill Meng", orderId.toString(), null);
        workflowAccessor.createProcessInstance(createProcessInstanceDto);

        ProcessInstance processInstance = runtimeService.createProcessInstanceQuery().processInstanceBusinessKey(orderId.toString(), processDefinitionKey).singleResult();

        Set<String> processVars = workflowAccessor.getProcessInstanceVariableNames(processInstance.getProcessInstanceId());

        assertEquals(1, processVars.size());

        TaskQuery taskQuery = taskService.createTaskQuery().processInstanceId(processInstance.getId());
        assertEquals(1, taskQuery.count());

        // First complete usertask1 audit
        List<Task> taskList = taskQuery.list();
        Task userTask1 = taskList.get(0);

        // Pass and need usertask2
        Map<String, String> workflowParams = new HashMap<String, String>();
        workflowParams.put("need_userTask2", "1");
        WorkflowOperations.CompleteTaskInstanceDto completeTaskInstanceDto = new WorkflowOperations.CompleteTaskInstanceDto(userTask1.getId(), "junit", workflowParams);
        workflowAccessor.completeTaskInstance(completeTaskInstanceDto);

        taskList = taskQuery.list();
        assertEquals(1, taskQuery.count());
        assertEquals(1, workflowParams.size());

        // Draw png ///////////////////////////////////////////
        ProcessMonitorChartInfoHelper helper = (ProcessMonitorChartInfoHelper) processEngineConfiguration.getBeans().get("processMonitorChartInfoHelper");
        ProcessMonitorChartInfoHelper.ChartInfo chartInfo = helper.getMonitorChartInfo(processInstance.getProcessInstanceId());

        String filePath = null;
        try {
            File tmpImage = File.createTempFile("simpleVariableEngineTraceTest", ".png");
            filePath = tmpImage.getAbsolutePath();
            FileImageOutputStream fios = new FileImageOutputStream(tmpImage);
            fios.write(chartInfo.getDiagramBytes());
            fios.flush();
            fios.close();
            fios = null;
        } catch (IOException ex) {
            Logger.getLogger(Draw2DProcessDiagramTest.class.getName()).log(Level.SEVERE, null, ex);
            assertEquals("Can not generate process diagram image.", true, false);
        }

        // Generate html image/map/area

    }

}