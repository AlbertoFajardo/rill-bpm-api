/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.api;

import com.clarkware.junitperf.ConstantTimer;
import com.clarkware.junitperf.LoadTest;
import com.clarkware.junitperf.Timer;
import java.util.logging.Level;
import java.util.logging.Logger;
import junit.extensions.TestSetup;
import junit.framework.Test;

/**
 *
 * @author mengran
 */
public class DifferProcessConcurrentTest {

    public DifferProcessConcurrentTest() {
    }

    public static Test suite() {

        int user = 30;
        Timer timer = new ConstantTimer(30);

        // Load test
        final PgSupportTest pgTest = new PgSupportTest();
        Test loadTest = new LoadTest(pgTest, user, timer);

        // Decorate load-test
        TestSetup wrapper = new TestSetup(loadTest) {

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
