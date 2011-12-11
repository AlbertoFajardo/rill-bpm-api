/*
 * To change this template, choose Tools | Templates
 * and open the template in the editor.
 */
package org.rill.bpm.api.support;

import java.util.HashMap;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Map;
import java.util.Set;

import org.apache.commons.lang.StringUtils;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.api.TaskExecutionContext;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.WorkflowOperations.XStreamSerializeHelper;
import org.rill.bpm.api.exception.ProcessException;
import org.rill.bpm.api.processvar.DummyOrderAudit;
import org.rill.bpm.api.support.XpathVarConvertTaskLifecycleInterceptor;


/**
 *
 * @author mengran
 */

public class XpathVarConvertTLITest {
 
    @Test
    public void xpathVarConvert() {
        
        XpathVarConvertTaskLifecycleInterceptor tli = new XpathVarConvertTaskLifecycleInterceptor();
        class XpathWorkflowAccessor implements WorkflowOperations {

            public List<String> createProcessInstance(String processDefinitionKey, String processStarter, String businessObjectId, Map<String, Object> startParams) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void terminalProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void suspendProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void resumeProcessInstance(String engineProcessInstanceId, String operator, String reason) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public List<String> completeTaskInstance(String engineTaskInstanceId, String operator, Map<String, Object> workflowParams) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Map<String, List<String>> batchCompleteTaskIntances(Map<String, Map<String, Object>> batchDTO, String operator) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public HashMap<String, String> getTaskInstanceInformations(String engineTaskInstanceId) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String getTaskNameByDefineId(String processDefinitionKey, String taskDefineId) {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public Set<String> getProcessInstanceVariableNames(String engineProcessInstanceId) {
                
                Set<String> varNames = new LinkedHashSet<String>();
                varNames.add("a");
                varNames.add("__b");
                varNames.add("__b_");
                varNames.add("__orderAudit_c");
                varNames.add("__orderAudit_auditAction");
                varNames.add("d");
                
                return varNames;
            }

            public void abortTaskInstance(String engineTaskInstanceId) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public String obtainTaskRole(String engineTaskInstanceId) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

            public void reassignTaskExecuter(String engineProcessInstanceId, String engineTaskInstanceId, String oldExecuter, String newExecuter) throws ProcessException {
                throw new UnsupportedOperationException("Not supported yet.");
            }

			@Override
			public String getEngineProcessInstanceIdByBOId(
					String businessObjectId, String processDefinitionKey)
					throws ProcessException {
				return null;
			}

			@Override
			public Set<String> getLastedVersionProcessDefinitionVariableNames(
					String processDefinitionKey) {
				return null;
			}
            
        }
        
        XpathWorkflowAccessor xpathWA = new XpathWorkflowAccessor();
        tli.setWorkflowAccessor(xpathWA);
        
        String xml = "<orderAudit><auditAction>1</auditAction><auditorName>mengran</auditorName></orderAudit>";
        DummyOrderAudit orderAudit = new DummyOrderAudit();
        orderAudit.setAuditAction(1);
        orderAudit.setAuditorName("mengran");
        Assert.assertEquals(xml, StringUtils.deleteWhitespace(XStreamSerializeHelper.serializeXml("orderAudit", orderAudit)));
        // Do convert
        TaskExecutionContext context = new TaskExecutionContext();
        Map<String, Object> workflowParams = new HashMap<String, Object>();
        workflowParams.put("orderAudit", orderAudit);
        workflowParams.put("a", "contains");
        context.setWorkflowParams(workflowParams);
        tli.preComplete(context);
        
        // Assert
        Assert.assertTrue(workflowParams.get("a").equals("contains"));
        Assert.assertTrue(!workflowParams.containsKey("__b"));
        Assert.assertTrue(!workflowParams.containsKey("__b_"));
        Assert.assertTrue(!workflowParams.containsKey("__orderAudit_c"));
        Assert.assertTrue(workflowParams.get("__orderAudit_auditAction").equals("1"));
        // Change strategy
        Assert.assertTrue(!"0".equals(workflowParams.get("d")));
    }
}
