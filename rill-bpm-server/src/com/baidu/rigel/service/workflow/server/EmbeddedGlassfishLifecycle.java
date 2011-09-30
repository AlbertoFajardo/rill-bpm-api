package com.baidu.rigel.service.workflow.server;

import java.util.List;
import java.util.Map;
import java.util.Map.Entry;
import java.util.Properties;
import java.util.logging.Logger;

import org.glassfish.embeddable.GlassFish;
import org.glassfish.embeddable.GlassFish.Status;
import org.glassfish.embeddable.GlassFishException;
import org.glassfish.embeddable.GlassFishProperties;
import org.glassfish.embeddable.GlassFishRuntime;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;

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

	public void start() {
		
		System.out.println("Operate system properties --Start");
		for (Entry<Object, Object> entry : System.getProperties().entrySet()) {
			System.out.println(entry.getKey() + " - " + System.getProperty(entry.getKey().toString()));
		}
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
