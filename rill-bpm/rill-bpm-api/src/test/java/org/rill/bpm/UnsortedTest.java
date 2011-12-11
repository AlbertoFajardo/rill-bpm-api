/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package org.rill.bpm;

import java.util.Iterator;
import java.util.Set;
import java.util.TreeSet;
import junit.framework.Assert;
import org.activiti.engine.impl.juel.Builder;
import org.activiti.engine.impl.juel.IdentifierNode;
import org.activiti.engine.impl.juel.Tree;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;

/**
 *
 * @author mengran
 */
public class UnsortedTest {

    public UnsortedTest() {
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

    // The methods must be annotated with annotation @Test. For example:
    //
    // @Test
    // public void hello() {}
    @Test
    public void jule() {

        Builder builder = new Builder();
        Tree tree = builder.build("${need_userTask2 == 1 && other_variable >= 3}");
        Iterable<IdentifierNode> listIdentifierNode = tree.getIdentifierNodes();
        Iterator<IdentifierNode> iterator = listIdentifierNode.iterator();
        Set<String> identifireNodeSet = new TreeSet<String>();
        Set<String> expectIdentifireNodeSet = new TreeSet<String>();
        expectIdentifireNodeSet.add("need_userTask2");
        expectIdentifireNodeSet.add("other_variable");
        while (iterator.hasNext()) {
           identifireNodeSet.add(iterator.next().toString());
        }
        Assert.assertEquals(expectIdentifireNodeSet, identifireNodeSet);
    }
    
    @Test
    public void testSplit() {
        
        String splitTarget = "abc_def";
        Assert.assertTrue(splitTarget.split("_").length == 2);
        splitTarget = "abc";
        Assert.assertTrue(splitTarget.split("_").length == 1);
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