/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */

package com.baidu.rigel.service.workflow;

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
public class JuelTest {

    public JuelTest() {
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

}