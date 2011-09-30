/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow;

import java.lang.reflect.Field;
import java.util.logging.Level;
import java.util.logging.Logger;
import org.activiti.engine.ProcessEngines;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Assert;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mengran
 */
public class StaticProtectedVarAccessTest {

    public StaticProtectedVarAccessTest() {
    }

    @BeforeClass
    public static void setUpClass() throws Exception {
    }

    @AfterClass
    public static void tearDownClass() throws Exception {
    }

    @Before
    public void setUp() {
    }

    @After
    public void tearDown() {
    }

    // TODO add test methods here.
    // The methods must be annotated with annotation @Test. For example:
    //
    @Test
    public void hello() {
        try {
            Field field = ProcessEngines.class.getDeclaredField("isInitialized");
            field.setAccessible(true);
            try {
            	synchronized (ProcessEngines.class) {
					boolean originalValue = field.getBoolean(ProcessEngines.class);
					field.setBoolean(ProcessEngines.class, !originalValue);
	                Assert.assertEquals(!originalValue, field.getBoolean(ProcessEngines.class));
	                // Set back to original value
	                field.setBoolean(ProcessEngines.class, originalValue);
				}
            } catch (IllegalArgumentException ex) {
                Logger.getLogger(StaticProtectedVarAccessTest.class.getName()).log(Level.SEVERE, null, ex);
            } catch (IllegalAccessException ex) {
                Logger.getLogger(StaticProtectedVarAccessTest.class.getName()).log(Level.SEVERE, null, ex);
            }
        } catch (NoSuchFieldException ex) {
            Logger.getLogger(StaticProtectedVarAccessTest.class.getName()).log(Level.SEVERE, null, ex);
        } catch (SecurityException ex) {
            Logger.getLogger(StaticProtectedVarAccessTest.class.getName()).log(Level.SEVERE, null, ex);
        }
    }

    @Test
    public void taskServiceInvokeExpression() {

        String expression = "taskNameAService.taskNameAComplete(com.baidu.rigel.service.workflow.DTO)";
        String[] beforeLeftBracket = expression.substring(0, expression.indexOf("(")).split("\\.");
        Assert.assertTrue("The expression parttern is [taskName]Service.[taskName]Complete(DTO.class.getName())", beforeLeftBracket.length == 2);
        String serviceName = beforeLeftBracket[0];
        String methodName = beforeLeftBracket[1];

        Assert.assertEquals(serviceName, "taskNameAService");
        Assert.assertEquals(methodName, "taskNameAComplete");

        String parameter = expression.substring(expression.indexOf("(") + 1, expression.indexOf(")"));
        Assert.assertEquals(parameter, "com.baidu.rigel.service.workflow.DTO");

        expression = "taskNameAService.taskNameAComplete()";
        String parameterEmpty = expression.substring(expression.indexOf("(") + 1, expression.indexOf(")"));
        Assert.assertEquals(parameterEmpty, "");

        int y1 = 380;
        int h1 = 55;
        int y2 = 377;
        int h2 = 60;
        Assert.assertTrue(Math.abs((y1 + h1/2) - (y2 + h2/2)) < 1);
    }
}
