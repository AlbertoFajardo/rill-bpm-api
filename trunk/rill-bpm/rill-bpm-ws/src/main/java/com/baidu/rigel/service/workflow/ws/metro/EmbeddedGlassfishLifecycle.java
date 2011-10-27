package com.baidu.rigel.service.workflow.ws.metro;

import java.util.HashMap;
import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.Stack;
import java.util.logging.Logger;

import javax.naming.Context;
import javax.naming.NameClassPair;
import javax.naming.NamingEnumeration;
import javax.naming.NamingException;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFish.Status;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.springframework.jndi.JndiCallback;
import org.springframework.jndi.JndiLocatorSupport;
import org.springframework.jndi.JndiTemplate;
import org.springframework.util.Assert;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;

/**
 * Embedded GF lifecycle controller with Spring.
 * 
 * @author mengran
 *
 */
public class EmbeddedGlassfishLifecycle {

	protected final Logger logger = Logger.getLogger(getClass().getName());
	
	private GlassFish _glassfish;
	private Properties properties;
	private Map<String, List<String>> initCommand;
	private boolean transferStringTypeOnly = true;
	
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

	public void setProperties(Properties properties) {
		this.properties = properties;
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
				parseResult.put(StringUtils.collectionToDelimitedString(xPath, "/"), envValue);
			}
			// Pop stack
			xPath.pop();
		}
		
	}

	public void start() throws Exception {
		
		System.out.println("Operate system properties --Start");
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			System.out.println(entry.getKey() + " - " + System.getProperty(entry.getKey().toString()));
		}
		
		// Cache tomcat's naming environment entry setting.(Generally configure it in context.xml)
		final Map<String, Object> existsNaming = new HashMap<String, Object>();
		new JndiTemplate().execute(new JndiCallback<Void>() {

			@Override
			public Void doInContext(Context ctx) throws NamingException {
				Context envContext = (Context) ctx.lookup(JndiLocatorSupport.CONTAINER_PREFIX);
				Stack<String> xPath = new Stack<String>();
				deeplyFirstParseNamingContext(existsNaming, xPath, envContext);
				return null;
			}
		});
		System.out.println("Get exists naming configurations " + ObjectUtils.getDisplayString(existsNaming));
		
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
			System.out.println("Clear naming related property " + prop + 
					", value " + System.getProperty(prop) + " from system properties.");
			System.clearProperty(prop);
		}
		System.out.println("----------------------------End");
		
		try {
			GlassFishProperties gfProperties = null;
			if (getProperties() != null) {
				logger.info("Start embedded GF with pre-config properties:" + getProperties());
				gfProperties = new GlassFishProperties(getProperties());
			} else {
				gfProperties = new GlassFishProperties();
			}
			_glassfish = GlassFishRuntime.bootstrap().newGlassFish(gfProperties);
			_glassfish.start();
			logger.info("Started embedded GF successfully.");
			
			// Run initialize command.
			if (!CollectionUtils.isEmpty(getInitCommand())) {
				for (Entry<String, List<String>> entry : getInitCommand().entrySet()) {
					logger.info("Run pre-config command " + entry.getKey() + " " + ObjectUtils.getDisplayString(entry.getValue()));
					_glassfish.getCommandRunner().run(entry.getKey(), 
							entry.getValue().toArray(new String[entry.getValue().size()]));
				}
			}
			
			// Re-bind tomcat's naming environment entry setting.(Generally configure it in context.xml)
			new JndiTemplate().execute(new JndiCallback<Void>() {

				@Override
				public Void doInContext(Context ctx) throws NamingException {
					for (Entry<String, Object> entry : existsNaming.entrySet()) {
						if (isTransferStringTypeOnly() && !(entry.getValue() instanceof String)) {
							System.out.println("NOT re-bind exists naming configuration " + ObjectUtils.getDisplayString(entry));
						} else {
							ctx.bind(entry.getKey(), entry.getValue());
							System.out.println("Re-bind exists naming configuration " + ObjectUtils.getDisplayString(entry));
						}
					}
					
					return null;
				}
			});
			
			// Test naming re-bind successfully(Only test string type at this version).
			new JndiTemplate().execute(new JndiCallback<Void>() {

				@Override
				public Void doInContext(Context ctx) throws NamingException {
					for (Entry<String, Object> entry : existsNaming.entrySet()) {
						if (isTransferStringTypeOnly() && !(entry.getValue() instanceof String)) {
							System.out.println("NOT test non-string type naming configuration " + ObjectUtils.getDisplayString(entry));
						} else {
							Assert.isTrue(entry.getValue().equals(ctx.lookup(entry.getKey())));
							System.out.println("SUCCESSFULLY Test string type naming configuration " + ObjectUtils.getDisplayString(entry));
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
