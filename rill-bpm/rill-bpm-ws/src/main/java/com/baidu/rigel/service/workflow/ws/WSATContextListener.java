package com.baidu.rigel.service.workflow.ws;

import java.util.logging.Logger;

import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;


/**
 * Prepare WSAT environment.
 * @author mengran
 *
 */
public class WSATContextListener implements ServletContextListener {

	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static final String WSAT_CONTEXT_ROOT = "WSAT_CONTEXT_ROOT";
	
	public void contextInitialized(ServletContextEvent sce) {

		String wsatContextRoot = sce.getServletContext().getContextPath(); 
		if (sce.getServletContext().getInitParameter(WSAT_CONTEXT_ROOT) != null) {
			wsatContextRoot = sce.getServletContext().getInitParameter(WSAT_CONTEXT_ROOT);
		}
		if (wsatContextRoot != null) {
			wsatContextRoot = wsatContextRoot.replaceAll("/", "");
			logger.info("Set WSAT_CONTEXT_ROOT to " + wsatContextRoot);
			System.setProperty(WSAT_CONTEXT_ROOT, wsatContextRoot);
		}
	}

	public void contextDestroyed(ServletContextEvent sce) {
		
		// Do nothing.
	}

}
