package nu.com.rill.analysis.report.cluster;

import java.io.IOException;
import java.net.Inet4Address;
import java.net.InetAddress;
import java.net.NetworkInterface;
import java.util.Enumeration;
import java.util.Set;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContextEvent;
import javax.servlet.ServletContextListener;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.springframework.jndi.JndiPropertySource;
import org.springframework.util.StringUtils;
import org.springframework.web.context.WebApplicationContext;
import org.springframework.web.context.support.StandardServletEnvironment;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.CookieGenerator;

/**
 * Must put first-SEQ in web.xml listener section
 * @author mengran
 *
 */
public class SessionStickyListenerAndFilter implements Filter, ServletContextListener {

	private static final Log LOGGER = LogFactory
			.getLog(SessionStickyListenerAndFilter.class);
	public static final String CACHE_NAME = "default";
	public static final String RE_SERVER_ID = "RE_SERVER_ID";
	public static final String RE_SERVER_CLUSTER_TCP_PING_KEY = "jgroups.tcpping.initial_hosts";
	public static final String RE_SERVER_CLUSTER_HOST_KEY = "jgroups.bind_addr";
	public static final String RE_SERVER_CLUSTER_PORT_KEY = "jgroups.tcp.port";
	public static final String SYSTEM_PROPERTIES_CONFIGURATION = "systemPropertiesConfigLocation";

//	private static final ThreadLocal<String> RE_SERVER_ID_HOLDER = new ThreadLocal<String>();
	private SpringEmbeddedCacheManager cacheManager;
	
	@Override
	public void contextInitialized(ServletContextEvent arg0) {
		
		JndiPropertySource jndiPropertySource = new JndiPropertySource(StandardServletEnvironment.JNDI_PROPERTY_SOURCE_NAME);
		
		Object pingKey = jndiPropertySource.getProperty(RE_SERVER_CLUSTER_TCP_PING_KEY);
		if (pingKey != null) {
			LOGGER.info("Add system property:jgroups.tcpping.initial_hosts=" + pingKey);
			System.setProperty(RE_SERVER_CLUSTER_TCP_PING_KEY, pingKey.toString());
		}
		Object portKey = jndiPropertySource.getProperty(RE_SERVER_CLUSTER_PORT_KEY);
		if (portKey != null) {
			LOGGER.info("Add system property:jgroups.tcp.port=" + portKey);
			System.setProperty(RE_SERVER_CLUSTER_PORT_KEY, portKey.toString());
		}
		
		if (System.getProperty(RE_SERVER_CLUSTER_TCP_PING_KEY) != null && 
        		System.getProperty(RE_SERVER_CLUSTER_HOST_KEY) == null ) {
			String hostIp = pickHost(jndiPropertySource);
			LOGGER.info("Add system property:jgroups.bind_addr=" + hostIp);
			System.setProperty(RE_SERVER_CLUSTER_HOST_KEY, hostIp);
        }
		
		String systemPropertiesConfiguration = arg0.getServletContext().getInitParameter(SYSTEM_PROPERTIES_CONFIGURATION);
		if (StringUtils.hasText(systemPropertiesConfiguration)) {
			Set<String> propertyName = StringUtils.commaDelimitedListToSet(systemPropertiesConfiguration);
			for (String p : propertyName) {
				LOGGER.info("Try to add system property:" + p);
				Object pValue = jndiPropertySource.getProperty(p);
				if (pValue != null) {
					LOGGER.info("Add system property:" + p + " " + pValue.toString());
					System.setProperty(p, pValue.toString());
				}
			}
		}
		
	}
	
	private String pickHost(JndiPropertySource jndiPropertySource) throws RuntimeException {
        String currentAddress = null;
        try {
            final Object localAddress = jndiPropertySource.getProperty(RE_SERVER_CLUSTER_HOST_KEY);
            if (localAddress != null) {
                currentAddress = InetAddress.getByName(localAddress.toString().trim()).getHostAddress();
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
            LOGGER.info("Pick host " + inetAddress.getHostAddress());
            return inetAddress.getHostAddress();
        } catch (Exception e) {
        	LOGGER.error(e.getMessage(), e);
            throw new RuntimeException(e);
        }
    }
	
	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
		WebApplicationContext context = WebApplicationContextUtils.getRequiredWebApplicationContext(arg0.getServletContext());
		
		cacheManager = (SpringEmbeddedCacheManager) context.getBean("cacheManager");
		cacheManager.getCache(CACHE_NAME);
	}

	private CookieGenerator cookieGenerator = new CookieGenerator();
	
	private String localPhysicalAddressHashcode() {
		
		Transport transport = cacheManager.getNativeCacheManager().getTransport();
		Address physicalAddress = transport.getPhysicalAddresses().get(0);
		
		return physicalAddress.hashCode() + "";
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {

		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		Cookie reServerIdCookie = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().toUpperCase().equals(RE_SERVER_ID)) {
					LOGGER.debug("Retrieve session sticky id from cookie "
							+ cookie.getValue() + " " + cookie + ". request " + request.getRequestURI());
					reServerIdCookie = cookie;
				}
			}
		}
		
		boolean needAddCookie = (reServerIdCookie == null);
		String localPhysical = localPhysicalAddressHashcode();
		if (reServerIdCookie != null && !localPhysical.equals(reServerIdCookie.getValue())) {
			LOGGER.warn("Session sticky maybe not effect, we will reset cookie value from " + reServerIdCookie + " to " + localPhysical);
			reServerIdCookie.setMaxAge(0);
			needAddCookie = true;
		}
		
		if (needAddCookie) {
			// Means first-time access
			cookieGenerator.setCookieName(RE_SERVER_ID);
			LOGGER.info("Add cookie value of session sticky id "
					+ localPhysical);
			cookieGenerator.addCookie(response, localPhysical);
		}

		arg2.doFilter(arg0, arg1);
	}

	@Override
	public void contextDestroyed(ServletContextEvent arg0) {
		
	}	

	@Override
	public void destroy() {

	}

	// @Override
	// public void put(Session sess) {
	//
	// cacheManager.getCache(CACHE_NAME).put(RE_JSESSION_ID_HOLDER.get(), sess);
	//
	// }
	//
	// @Override
	// public Session get(Object navsess) {
	//
	// if (RE_JSESSION_ID_HOLDER.get() != null) {
	// LOGGER.debug("Use session key " + RE_JSESSION_ID_HOLDER.get() +
	// " to find session in cache.");
	// } else {
	// // Maybe not HTTP request
	// LOGGER.debug("Maybe not HTTP request, so return null");
	// return null;
	// }
	//
	// ValueWrapper vw =
	// cacheManager.getCache(CACHE_NAME).get(RE_JSESSION_ID_HOLDER.get());
	// return vw == null ? null : (Session) vw.get();
	// }

}
