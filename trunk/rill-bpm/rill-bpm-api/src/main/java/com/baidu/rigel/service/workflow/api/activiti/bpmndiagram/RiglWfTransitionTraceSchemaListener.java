package com.baidu.rigel.service.workflow.api.activiti.bpmndiagram;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;

import com.baidu.rigel.service.workflow.api.activiti.RillProcessEngineConfiguration.SchemaOperationEventListener;

/**
 * Schema operation listener for RIGEL_WF_*, it's noticed during process engine creating.
 * @author mengran
 *
 */
public class RiglWfTransitionTraceSchemaListener extends SchemaOperationEventListener {

	@Override
	public void onSchemaCreate(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
		
		// RIGEL_WF_* DB initialize
		commandExecutor.execute(new Command<Boolean>() {

			public Boolean execute(CommandContext commandContext) {

				boolean tablePresent = commandContext.getDbSqlSession()
						.isTablePresent("RIGEL_WF_TRANSITION_TAKE_TRACE");
				if (tablePresent) {
					return true;
				}

				// Do create
				String resourceName = getResourceForDbOperation(
						commandContext.getDbSqlSession(), "create", "create",
						"wf");
				commandContext.getDbSqlSession().executeSchemaResource(
						"create", "wf", resourceName, false);
				return true;
			}

			String getResourceForDbOperation(DbSqlSession dbSqlSession,
					String directory, String operation, String component) {
				String databaseType = dbSqlSession.getDbSqlSessionFactory()
						.getDatabaseType();
				return "com/baidu/rigel/service/workflow/db/" + directory
						+ "/rigel." + databaseType + "." + operation + "."
						+ component + ".sql";
			}
		});
        
	}
	
}
