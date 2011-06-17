/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api;

import com.clarkware.junitperf.ConstantTimer;
import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.Timer;
import java.io.IOException;
import java.lang.reflect.Field;
import java.net.URL;
import java.util.Collections;
import java.util.Enumeration;
import java.util.List;
import java.util.Random;
import java.util.TimerTask;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.extensions.TestSetup;
import junit.framework.Test;
import junit.framework.TestResult;
import org.activiti.engine.ActivitiException;
import org.activiti.engine.ProcessEngines;
import org.activiti.engine.impl.util.ReflectUtil;
import org.activiti.engine.runtime.ProcessInstance;
import org.activiti.engine.task.TaskQuery;
import org.activiti.engine.test.Deployment;

/**
 *
 * @author mengran
 */
public class ContinuousPerformanceTests {

    public ContinuousPerformanceTests() {
    }

    private static class TestTimerTask extends TimerTask {

        private PgSupportTest testHolder;
        private TestResult testResultHolder;

        public TestTimerTask(PgSupportTest test, TestResult testResult) {
            testHolder = test;
            testResultHolder = testResult;
        }

        @Override
        public void run() {

            // Run test case
            int user = (new Random()).nextInt(50) + 1;
            Timer timer = new ConstantTimer(30);
            final Test loadTest = new LoadTest(testHolder, user, timer);
            loadTest.run(testResultHolder);
        }
    }

    public static Test suite() {

        // Load test
        final PgSupportTest pgTest = new PgSupportTest() {

            @Override
            protected void initializeProcessEngine() {
                if (cachedProcessEngine == null) {
                    ClassLoader classLoader = ReflectUtil.getClassLoader();
                    Enumeration<URL> resources = null;
                    try {
                        resources = classLoader.getResources("com/baidu/rigel/service/workflow/api/continuous.activiti.cfg.xml");
                    } catch (IOException e) {
                        throw new ActivitiException("problem retrieving continuous.activiti.cfg.xml resources on the classpath: " + System.getProperty("java.class.path"), e);
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
                    cachedProcessEngine = ProcessEngines.getDefaultProcessEngine();
                    if (cachedProcessEngine == null) {
                        throw new ActivitiException("no default process engine available");
                    }
                }
                processEngine = cachedProcessEngine;
            }

            @Override
            public void endProcess(TaskQuery taskQuery, ProcessInstance processInstance,
                    long startTime, List<Long> perTaskTimeCostList) {

                long timeCost = System.currentTimeMillis() - startTime;
                Collections.sort(perTaskTimeCostList);
                long perTaskTimeCostSum = 0;
                for (Long perTaskTimeCost : perTaskTimeCostList) {
                    perTaskTimeCostSum += perTaskTimeCost;
                }
                long perTaskTimeCostAvg = perTaskTimeCostSum / perTaskTimeCostList.size();
                // Do not end process for process data cumulation
                ReflectUtil.invoke(getWorkflowAccessor(), "runExtraCommand", 
                        new Object[] {new ContinousPerformanceResultBuild(timeCost, perTaskTimeCostList.get(perTaskTimeCostList.size() -1),
                                perTaskTimeCostList.get(0), perTaskTimeCostAvg)});
            }

            @Override
            @Deployment(resources = {"com/baidu/rigel/service/workflow/api/pg-support.bpmn20.xml"})
            public void panguDeploy() {
                // Do nothing
            }

        };
        final java.util.Timer timerSchedule = new java.util.Timer();

        // Decorate pg-test
        TestSetup wrapper = new TestSetup(pgTest) {

            @Override
            protected void setUp() {
                pgTest.deploy();
            }

            @Override
            public void basicRun(TestResult result) {

                // Do run
                TestTimerTask ttt = new TestTimerTask(pgTest, result);
                timerSchedule.schedule(ttt, 5 * 1000, 30 * 1000);
                while (true) {
                    try {
                        Thread.sleep(5 * 1000 * 60);
                    } catch (InterruptedException ex) {
                        Logger.getLogger(ContinuousPerformanceTests.class.getName()).log(Level.SEVERE, null, ex);
                    }
                }
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
