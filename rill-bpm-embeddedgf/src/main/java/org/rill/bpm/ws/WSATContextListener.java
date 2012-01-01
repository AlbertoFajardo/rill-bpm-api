package org.rill.bpm.ws;

import java.io.File;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.servlet.ServletContext;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;

import com.sun.enterprise.glassfish.bootstrap.Constants;
import com.sun.xml.ws.tx.at.WSATConstants;
import com.sun.xml.ws.tx.dev.WSATRuntimeConfig;


/**
 * Prepare WSAT environment.
 * @author mengran
 *
 */
/**
 * @author mengran
 *
 */
/**
 * @author mengran
 *
 */
public class WSATContextListener implements ServletContextListener {

	protected final Logger logger = Logger.getLogger(this.getClass().getName());
	
	public static final String WSAT_CONTEXT_ROOT = "WSAT_CONTEXT_ROOT";
	public static final String WSAT_HTTP_PORT = "HTTP_PORT";
	
	public void contextInitialized(final ServletContextEvent sce) {

		// WSAT context root property setting----------
		String wsatContextRoot = sce.getServletContext().getContextPath(); 
		if (sce.getServletContext().getInitParameter(WSAT_CONTEXT_ROOT) != null) {
			wsatContextRoot = sce.getServletContext().getInitParameter(WSAT_CONTEXT_ROOT);
		}
		if (wsatContextRoot != null) {
			wsatContextRoot = wsatContextRoot.replaceAll("/", "");
			logger.info("Set WSAT_CONTEXT_ROOT to " + wsatContextRoot);
			System.setProperty(WSAT_CONTEXT_ROOT, wsatContextRoot);
		}
		logger.info("WSAT context root: " + WSATConstants.WSAT_CONTEXT_ROOT);
		logger.info(WSATConstants.WSAT_COORDINATORPORTTYPEPORT);
		logger.info(WSATConstants.WSAT_REGISTRATIONCOORDINATORPORTTYPEPORT);
		logger.info(WSATConstants.WSAT_REGISTRATIONREQUESTERPORTTYPEPORT);
		logger.info(WSATConstants.WSAT_PARTICIPANTPORTTYPEPORT);
		
//		System.setProperty("com.sun.xml.ws.transport.http.client.HttpTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.ws.transport.local.LocalTransportPipe.dump", "true");
//		System.setProperty("com.sun.xml.ws.util.pipe.StandaloneTubeAssembler.dump", "true");
//		System.setProperty("com.sun.xml.ws.transport.http.HttpAdapter.dump", "true");
		
		// WSAT web service context root----------
		final WSATRuntimeConfig.TxlogLocationProvider txlogLocationProvider = new WSATRuntimeConfig.TxlogLocationProvider() {
            public String getTxLogLocation() {
				try {
					String instanceRootPath = System.getProperty(Constants.INSTANCE_ROOT_PROP_NAME);
					File txLogFile = new File(instanceRootPath, "txLog/");
					txLogFile.mkdir();
					return txLogFile.getAbsolutePath();
				} catch (Exception ex) {
					logger.log(Level.WARNING, "Can not create txLog dir in instance root. Use servlet context real path, cause trouble when run two embedded-GFs in debug-mode.", ex);
					try {
						return sce.getServletContext().getRealPath("/");
					} catch (Exception e) {
						logger.log(Level.WARNING, "Can not findservlet context real path and then return null", e);
						return null;
					}
				}
            }
        };
        
        WSATRuntimeConfig.initializer()
        		.httpPort(pickPort(sce.getServletContext()))
                .txLogLocation(txlogLocationProvider)
                .hostName(pickHost())
                .done();
	}
	
	/**
	 * <ul>
	 * 	<li> First use <code>context-param</code> configuration
	 * 	<li> Second use 8080 directly
	 * @param sc servlet context
	 * @return HTTP port
	 */
	protected String pickPort(ServletContext sc) {
		
		return sc.getInitParameter("HTTP_PORT") == null ? "8080" : sc.getInitParameter(WSAT_HTTP_PORT).trim();
	}
	
	private String pickHost() throws RuntimeException {
        String currentAddress = null;
        try {
            final String localAddress = System.getProperty("hazelcast.local.localAddress");
            if (localAddress != null) {
                currentAddress = InetAddress.getByName(localAddress.trim()).getHostAddress();
            }
            if (currentAddress == null) {
	            final Enumeration<NetworkInterface> enums = NetworkInterface.getNetworkInterfaces();
	            interfaces:
	            while (enums.hasMoreElements()) {
	                final NetworkInterface ni = enums.nextElement();
	                final Enumeration<InetAddress> e = ni.getInetAddresses();
	                while (e.hasMoreElements()) {
	                    final InetAddress inetAddress = e.nextElement();
	                    if (inetAddress instanceof Inet4Address) {
	                        if (!inetAddress.isLoopbackAddress()) {
	                            currentAddress = inetAddress.getHostAddress();
	                            break interfaces;
	                        }
	                    }
	                }
	            }
            }
            if (currentAddress == null) {
                currentAddress = "127.0.0.1";
            }
            final InetAddress inetAddress = InetAddress.getByName(currentAddress);
            return inetAddress.getHostAddress();
        } catch (Exception e) {
            logger.log(Level.SEVERE, e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }

	public void contextDestroyed(ServletContextEvent sce) {
		
		// Do nothing.
	}

}
