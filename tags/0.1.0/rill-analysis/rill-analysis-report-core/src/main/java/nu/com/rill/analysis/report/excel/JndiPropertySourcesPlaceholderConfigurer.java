package nu.com.rill.analysis.report.excel;

import org.springframework.context.support.PropertySourcesPlaceholderConfigurer;
import org.springframework.core.env.AbstractEnvironment;
import org.springframework.core.env.Environment;
import org.springframework.jndi.JndiLocatorDelegate;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.web.context.support.StandardServletEnvironment;

public class JndiPropertySourcesPlaceholderConfigurer extends
		PropertySourcesPlaceholderConfigurer {
	
	@Override
	public void setEnvironment(Environment environment) {
		
		super.setEnvironment(environment);
		
		// Add JNDI context support
		if (JndiLocatorDelegate.isDefaultJndiEnvironmentAvailable()) {
			((AbstractEnvironment) environment).getPropertySources().addFirst(new JndiPropertySource(StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME));
		}
	}
	
}
