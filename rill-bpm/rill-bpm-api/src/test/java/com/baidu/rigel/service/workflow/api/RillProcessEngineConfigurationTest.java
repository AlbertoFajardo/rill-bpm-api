package com.baidu.rigel.service.workflow.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

import com.baidu.rigel.service.workflow.api.activiti.ActivitiAccessor;
import com.baidu.rigel.service.workflow.api.activiti.RillProcessEngineConfiguration.SchemaOperationEventListener;

// FIXME: Need test all schema operation.
@ContextConfiguration(value="classpath:com/baidu/rigel/service/workflow/api/schemaoperation.activiti.cfg.xml")
public class RillProcessEngineConfigurationTest extends AbstractJUnit4SpringContextTests {
	
	public static class DummySchemaOperationListener extends SchemaOperationEventListener {

		static List<String> methodCalled = new ArrayList<String>();
		
		@Override
		public void onSchemaCreate(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
			methodCalled.add("onSchemaCreate");
		}

		@Override
		public void onSchemaUpdate(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
			methodCalled.add("onSchemaUpdate");
		}

		@Override
		public void onSchemaPrune(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
			methodCalled.add("onSchemaPrune");
		}

		@Override
		public void onSchemaDrop(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
			
			methodCalled.add("onSchemaDrop");
		}
		
	}
	
	@Resource
	private WorkflowOperations workflowAccessor;
	
	@Test
	public void checkSchemaOperationEvent() {
		
//		Assert.assertTrue(DummySchemaOperationListener.methodCalled.contains("onSchemaUpdate"));
		Assert.assertTrue(DummySchemaOperationListener.methodCalled.contains("onSchemaCreate"));
		
		ActivitiAccessor activitiAccessor = ActivitiAccessor.retrieveActivitiAccessorImpl(workflowAccessor, ActivitiAccessor.class);
		
		Boolean traceTableExists = activitiAccessor.runExtraCommand(new Command<Boolean>() {

            public Boolean execute(CommandContext commandContext) {

                boolean tablePresent = commandContext.getDbSqlSession().isTablePresent("RIGEL_WF_TRANSITION_TAKE_TRACE");
                if (tablePresent) {
                    return true;
                }

                return false;
            }
        });
		Assert.assertTrue(traceTableExists);
		
	}
}
