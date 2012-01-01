package org.rill.bpm.ws.listener;

import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;

import org.apache.catalina.ServerFactory;
import org.apache.catalina.Service;
import org.apache.catalina.connector.Connector;
import org.rill.bpm.ws.WSATContextListener;

public class WSATServerPickPortListener extends WSATContextListener {

	private static final Logger logger = Logger.getLogger(WSATServerPickPortListener.class.getName());
	
	@Override
	protected String pickPort(ServletContext sc) {
		
		logger.info("Try to pick port from tomcat...");
		String port = null;
		try {
			port = internalPickPort();
			logger.info("Pick port: " + port + " from tomcat.");
		} catch (Exception e) {
			logger.log(Level.WARNING, "Can not pick port from tomcat, return super's result fallback.", e);
			port = super.pickPort(sc);
			logger.info("Pick port: " + port + " from super's implementation.");
		}
		
		logger.info("Pick port: " + port + " and use it for WSAT.");
		return port;
	}
	
	private String internalPickPort() {
		
		Service catalinaService = ServerFactory.getServer().findService("Catalina");
		for (Connector connector : catalinaService.findConnectors()) {
			if (connector.getProtocol().equals("HTTP/1.1")) {
				return new Integer(connector.getPort()).toString();
			}
		}
		
		throw new UnsupportedOperationException("WSAT has test againest Tomcat 6.0, Please change to it.");
	}

	
}
