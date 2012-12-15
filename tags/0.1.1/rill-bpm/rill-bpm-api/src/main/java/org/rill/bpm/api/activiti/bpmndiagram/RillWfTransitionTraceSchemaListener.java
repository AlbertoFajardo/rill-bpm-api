package org.rill.bpm.api.activiti.bpmndiagram;

import org.activiti.engine.impl.db.DbSqlSession;
import org.activiti.engine.impl.interceptor.Command;
import org.activiti.engine.impl.interceptor.CommandContext;
import org.activiti.engine.impl.interceptor.CommandExecutor;
import org.rill.bpm.api.activiti.RillProcessEngineConfiguration.SchemaOperationEventListener;


/**
 * Schema operation listener for RILL_WF_*, it's noticed during process engine creating.
 * @author mengran
 *
 */
public class RillWfTransitionTraceSchemaListener extends SchemaOperationEventListener {

	@Override
	public void onSchemaCreate(CommandExecutor commandExecutor, DbSqlSession dbSqlSession) {
		
		// RILL_WF_* DB initialize
		commandExecutor.execute(new Command<Boolean>() {

			public Boolean execute(CommandContext commandContext) {

				boolean tablePresent = commandContext.getDbSqlSession()
						.isTablePresent("RILL_WF_TRANSITION_TAKE_TRACE");
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
				return "org/rill/bpm/db/" + directory
						+ "/rill." + databaseType + "." + operation + "."
						+ component + ".sql";
			}
		});
        
	}
	
}
