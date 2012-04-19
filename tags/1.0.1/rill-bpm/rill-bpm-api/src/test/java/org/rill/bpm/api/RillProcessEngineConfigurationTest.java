package org.rill.bpm.api;

import java.util.ArrayList;
import java.util.List;

import javax.annotation.Resource;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.api.WorkflowOperations;
import org.rill.bpm.api.activiti.ActivitiAccessor;
import org.rill.bpm.api.activiti.RillProcessEngineConfiguration.SchemaOperationEventListener;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


// FIXME: Need test all schema operation.
@ContextConfiguration(value="classpath:org/rill/bpm/api/activiti.cfg.xml")
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

                boolean tablePresent = commandContext.getDbSqlSession().isTablePresent("RILL_WF_TRANSITION_TAKE_TRACE");
                if (tablePresent) {
                    return true;
                }

                return false;
            }
        });
		Assert.assertTrue(traceTableExists);
		
	}
}
