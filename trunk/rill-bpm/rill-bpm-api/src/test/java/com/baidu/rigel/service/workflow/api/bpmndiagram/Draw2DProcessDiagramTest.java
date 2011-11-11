/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api.bpmndiagram;

import com.baidu.rigel.service.workflow.api.DifferProcessConcurrentTest;
import com.baidu.rigel.service.workflow.api.PgSupportTest;
import com.baidu.rigel.service.workflow.api.activiti.ActivitiTemplate;
import com.baidu.rigel.service.workflow.api.activiti.bpmndiagram.ProcessMonitorChartInfoHelper;
import java.io.File;
import java.io.IOException;
import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;
import javax.imageio.stream.FileImageOutputStream;
import junit.extensions.TestSetup;
import junit.framework.Test;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 *
 * @author mengran
 */
public class Draw2DProcessDiagramTest {

    public Draw2DProcessDiagramTest() {
    }

    public static Test suite() {

        // Load test
        final PgSupportTest pgTest = new PgSupportTest() {

            @Override
            public void endProcess(TaskQuery taskQuery, ProcessInstance processInstance,
                    long startTime, List<Long> perTaskTimeCostList) {

                // Generate process diagrame image bytes
                ProcessMonitorChartInfoHelper helper = new ProcessMonitorChartInfoHelper();
                helper.setWorkflowAccessor((ActivitiTemplate) getWorkflowAccessor());
                ProcessMonitorChartInfoHelper.ChartInfo chartInfo = helper.getMonitorChartInfo(processInstance.getProcessInstanceId()).get(processInstance.getProcessInstanceId());
                try {
                    File tmpImage = File.createTempFile("drawProcessDiagramTest", ".png");
                    FileImageOutputStream fios = new FileImageOutputStream(tmpImage);
                    fios.write(chartInfo.getDiagramBytes());
                    fios.flush();
                    fios.close();
                    fios = null;
                } catch (IOException ex) {
                    Logger.getLogger(Draw2DProcessDiagramTest.class.getName()).log(Level.SEVERE, null, ex);
                    assertEquals("Can not generate process diagram image.", true, false);
                }
                
            }

            @Override
            @Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support.bpmn20.xml"})
            public void panguDeploy() {
                // Do nothing
            }
        };

        // Decorate pg-test
        TestSetup wrapper = new TestSetup(pgTest) {

            @Override
            protected void setUp() {
                pgTest.deploy();
            }

            @Override
            protected void tearDown() {
                try {
                    pgTest.undeploy();
                } catch (Throwable ex) {
                    Logger.getLogger(DifferProcessConcurrentTest.class.getName()).log(Level.SEVERE, "Fail to call undeploy.", ex);
                }
            }
        };

        return wrapper;
    }
}
