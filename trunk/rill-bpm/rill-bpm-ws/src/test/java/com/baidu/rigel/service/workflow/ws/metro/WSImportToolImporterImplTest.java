/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package com.baidu.rigel.service.workflow.ws.metro;

import junit.framework.Assert;
import org.junit.After;
import org.junit.AfterClass;
import org.junit.Before;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.core.io.ClassPathResource;

/**
 * Coding on eclipse platform. Commit successfully, Great~~
 *
 * @author mengran
 */
public class WSImportToolImporterImplTest {
    
    public WSImportToolImporterImplTest() {
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
    public void doImport() {
        
        WSImportToolImporterImpl importer = new WSImportToolImporterImpl();
        ClassPathResource cpr = new ClassPathResource("BPMWebService.wsdl");
        importer.importFrom(cpr.getPath());
        Assert.assertTrue(importer.getServices().size() == 1);
        Assert.assertTrue(importer.getServices().iterator().next().getName().equals("RemoteActivitiTemplateService"));
    }
}
