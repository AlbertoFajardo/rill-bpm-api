package org.rill.bpm.ws.metro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Level;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.InitialContext;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.glassfish.embeddable.CommandResult;
import org.glassfish.embeddable.CommandResult.ExitStatus;
import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFish.Status;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.springframework.util.ResourceUtils;

import com.sun.enterprise.glassfish.bootstrap.Constants;
import com.sun.enterprise.glassfish.bootstrap.JarUtil;
import com.sun.enterprise.util.StringUtils;

/**
 * Embedded GF lifecycle controller with Spring.
 * 
 * @author mengran
 *
 */
public class EmbeddedGlassfishLifecycle {

	public static final String CONTAINER_PREFIX = "java:comp/env/";
	
	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private GlassFish _glassfish;
	private Properties properties;
	private Map<String, List<String>> initCommand;
	private boolean transferStringTypeOnly = true;
	private boolean keepAlive = false;
	private boolean enableBTrace = false;
	
	public final boolean isEnableBTrace() {
		return enableBTrace;
	}

	public final void setEnableBTrace(boolean enableBTrace) {
		this.enableBTrace = enableBTrace;
	}

	public final boolean isKeepAlive() {
		return keepAlive;
	}

	public final void setKeepAlive(boolean keepAlive) {
		this.keepAlive = keepAlive;
	}

	public final boolean isTransferStringTypeOnly() {
		return transferStringTypeOnly;
	}
	
	/**
	 * <p>
	 * 	This version not support this property's configuration
	 * @param transferStringTypeOnly If you want to transfer non-string type naming configuration, set it to false.
	 */
	public final void setTransferStringTypeOnly(boolean transferStringTypeOnly) {
		this.transferStringTypeOnly = transferStringTypeOnly;
		throw new UnsupportedOperationException("This version not support this property's configuration");
	}

	public final Map<String, List<String>> getInitCommand() {
		return initCommand;
	}

	public final void setInitCommand(Map<String, List<String>> initCommand) {
		this.initCommand = initCommand;
	}

	public Properties getProperties() {
		return properties;
	}

	public void setProperties(Properties properties) throws Exception {
		this.properties = properties;
		for (Entry<Object, Object> entry : this.properties.entrySet()) {
			if (ResourceUtils.isUrl(entry.getValue().toString())) {
				entry.setValue(ResourceUtils.getFile(entry.getValue().toString()).toURI().toString());
			}
		}
	}

	public final GlassFish getGlassfish() {
		return _glassfish;
	}
	
	private void deeplyFirstParseNamingContext(Map<String, Object> parseResult, Stack<String> xPath , Context envContext) throws NamingException {
		
		NamingEnumeration<NameClassPair> ne = envContext.list("/");
		while (ne.hasMoreElements()) {
			String envName = ne.nextElement().getName();
			// Push stack
			xPath.push(envName);
			Object envValue = envContext.lookup(envName);
			if (envValue instanceof Context) {
				deeplyFirstParseNamingContext(parseResult, xPath, (Context) envValue);
			} else {
				parseResult.put(StringUtils.cat("/", xPath.toArray(new String[xPath.size()])), envValue);
			}
			// Pop stack
			xPath.pop();
		}
		
	}
	
	private interface JndiAccessorCallBack<T> {
		
		T doAccessJndiContext(Context ctx) throws NamingException;
	}
	
	private <T> T jndiAccessor(JndiAccessorCallBack<T> callBack) {
		
		Context ctx = null;
		T t = null;
		try {
			ctx = new InitialContext();
			t = callBack.doAccessJndiContext(ctx);
			
		} catch (NamingException e) {
			logger.log(Level.INFO, "Could not access JNDI InitialContext", e);
		} finally {
			if (ctx != null) {
				try {
					ctx.close();
				}
				catch (NamingException ex) {
					logger.log(Level.INFO, "Could not close JNDI InitialContext", ex);
				}
			}
		}
		
		return t;
	}

	public void start() throws Exception {
		
		logger.info("Operate system properties --Start");
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			logger.finer(entry.getKey() + " - " + System.getProperty(entry.getKey().toString()));
		}
		
		// Cache tomcat's naming environment entry setting.(Generally configure it in context.xml)
		final Map<String, Object> existsNaming = new HashMap<String, Object>();
		jndiAccessor(new JndiAccessorCallBack<Void>() {

			@Override
			public Void doAccessJndiContext(Context ctx) throws NamingException {
				Context envContext = (Context) ctx.lookup(CONTAINER_PREFIX);
				Stack<String> xPath = new Stack<String>();
				deeplyFirstParseNamingContext(existsNaming, xPath, envContext);
				return null;
			}
		});
		logger.info("Get exists naming configurations " + existsNaming);
		
		// Set keep-alive
		String originalKeepAlive = System.getProperty("http.keepAlive");
		logger.info("Change http.keepAlive from " + (originalKeepAlive == null ? "null" : originalKeepAlive) + " to " + isKeepAlive());
		System.setProperty("http.keepAlive", new Boolean(isKeepAlive()).toString());
		
		// Use embedded GF's naming implementation
		final String[] PROPS = new String[] {
			javax.naming.Context.INITIAL_CONTEXT_FACTORY,
			javax.naming.Context.OBJECT_FACTORIES,
			javax.naming.Context.URL_PKG_PREFIXES,
			javax.naming.Context.STATE_FACTORIES,
			javax.naming.Context.PROVIDER_URL,
			javax.naming.Context.DNS_URL,
			// The following shouldn't create a runtime dependence on ldap package.
			javax.naming.ldap.LdapContext.CONTROL_FACTORIES
		    };
		for (String prop : PROPS) {
			logger.info("Clear naming related property " + prop + 
					", value " + System.getProperty(prop) + " from system properties.");
			System.clearProperty(prop);
		}
		logger.info("----------------------------End");
		
		try {
			GlassFishProperties gfProperties = null;
			if (getProperties() != null) {
				logger.info("Start embedded GF with pre-config properties:" + getProperties());
				gfProperties = new GlassFishProperties(getProperties());
			} else {
				gfProperties = new GlassFishProperties();
			}
			_glassfish = GlassFishRuntime.bootstrap().newGlassFish(gfProperties);
			
//			logger.info("Extract logging.properties for GFFileHandler");
//			// Add by MENGRAN at 2012-02-29 for GFFileHandler
//			File configFileDir = new File(System.getProperty(Constants.INSTALL_ROOT_PROP_NAME));
//			for (File f : configFileDir.listFiles()) {
//				if (f.isDirectory() && f.getName().startsWith("config")) {
//					configFileDir = f;
//					break;
//				}
//			}
//			StaticGlassFishRuntime.copy(getClass().getClassLoader().getResource("logging.properties"), configFileDir, true);
			
			if (isEnableBTrace()) {
				logger.info("Extract btract-agent rar to enable monitor feature");
				// Add by MENGRAN at 2012-02-28 for enable monitor feature
				JarUtil.extractRar(System.getProperty(Constants.INSTALL_ROOT_PROP_NAME), "monitor");
			}
			
			_glassfish.start();
			logger.info("Started embedded GF successfully.");
			
			// Run initialize command.
			if (getInitCommand() != null && !getInitCommand().isEmpty()) {
				for (Entry<String, List<String>> entry : getInitCommand().entrySet()) {
					logger.info("Run pre-config command " + entry.getKey() + " " + entry.getValue());
					CommandResult commandResult = _glassfish.getCommandRunner().run(entry.getKey(), 
							entry.getValue().toArray(new String[entry.getValue().size()]));
					if (commandResult.getExitStatus().equals(ExitStatus.FAILURE)) {
						logger.log(Level.SEVERE, "Run command fail. " + commandResult.getOutput(), commandResult.getFailureCause());
					} else {
						logger.info("Run command successfully. " + commandResult.getOutput());
					}
				}
			}
			
			// Re-bind tomcat's naming environment entry setting.(Generally configure it in context.xml)
			jndiAccessor(new JndiAccessorCallBack<Void>() {

				@Override
				public Void doAccessJndiContext(Context ctx) throws NamingException {
					for (Entry<String, Object> entry : existsNaming.entrySet()) {
						if (isTransferStringTypeOnly() && !(entry.getValue() instanceof String)) {
							logger.info("NOT re-bind exists naming configuration " + entry);
						} else {
							ctx.bind(entry.getKey(), entry.getValue());
							logger.info("Re-bind exists naming configuration " + entry);
						}
					}
					
					return null;
				}
			});
			
			// Test naming re-bind successfully(Only test string type at this version).
			jndiAccessor(new JndiAccessorCallBack<Void>() {

				@Override
				public Void doAccessJndiContext(Context ctx) throws NamingException {
					for (Entry<String, Object> entry : existsNaming.entrySet()) {
						if (isTransferStringTypeOnly() && !(entry.getValue() instanceof String)) {
							logger.info("NOT test non-string type naming configuration " + entry);
						} else {
							Object jndiObject = ctx.lookup(entry.getKey());
							if (entry.getValue().equals(jndiObject)) {
								logger.info("SUCCESSFULLY Test string type naming configuration " + entry);
							} else {
								throw new IllegalStateException("Fail to test string type naming configuration " + entry.getKey() 
										+ ", espect " + entry.getValue() + " actual " + jndiObject);
							}
						}
					}
					
					return null;
				}
				
			});
			
		} catch (GlassFishException e) {
			throw new RuntimeException(e);
		}
	}

	public void stop() {

		try {
			_glassfish.stop();
			_glassfish.dispose();
		} catch (GlassFishException e) {
			throw new RuntimeException(e);
		}
		
	}

	public boolean isRunning() {
		
		try {
			return _glassfish != null && _glassfish.getStatus().equals(Status.STARTED);
		} catch (GlassFishException e) {
			throw new RuntimeException(e);
		}
	}
	
}
