package nu.com.rill.analysis.report.cluster;

import java.io.IOException;
import java.util.UUID;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpServletResponse;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.util.Assert;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.springframework.web.util.CookieGenerator;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.sys.SessionCache;

public class InfinispanSessionCache implements SessionCache, Filter {

	private static final Log LOGGER = LogFactory.getLog(InfinispanSessionCache.class);
	public static final String CACHE_NAME = "default";
	public static final String RE_JSESSIONID = "RE_SESSION_CACHE_ID";
	
	private static final ThreadLocal<String> RE_JSESSION_ID_HOLDER = new ThreadLocal<String>();
	private SpringEmbeddedCacheManager cacheManager;

	@Override
	public void init(WebApp wapp) {
		
		cacheManager = (SpringEmbeddedCacheManager) WebApplicationContextUtils.getRequiredWebApplicationContext(
					(ServletContext) wapp.getNativeContext()).getBean("cacheManager");
	}

	@Override
	public void destroy(WebApp wapp) {
		
	}

	@Override
	public void remove(Session sess) {
		LOGGER.debug("Session destoryed but we do nothing at this implementation.");
	}

	@Override
	public void put(Session sess) {
		
		Assert.notNull(RE_JSESSION_ID_HOLDER.get(), "Must in HTTP request context to put session into cache");
		
		LOGGER.debug("Put session into cache using key " + RE_JSESSION_ID_HOLDER.get());
		cacheManager.getCache(CACHE_NAME).put(RE_JSESSION_ID_HOLDER.get(), sess);
		
	}

	@Override
	public Session get(Object navsess) {
		
		if (RE_JSESSION_ID_HOLDER.get() != null) {
			LOGGER.debug("Use session key " + RE_JSESSION_ID_HOLDER.get() + " to find session in cache.");
		} else {
			// Maybe not HTTP request
			LOGGER.debug("Maybe not HTTP request, so return null");
			return null;
		}
		
		ValueWrapper vw = cacheManager.getCache(CACHE_NAME).get(RE_JSESSION_ID_HOLDER.get());
		return vw == null ? null : (Session) vw.get();
	}

	@Override
	public void destroy() {
		
	}

	private CookieGenerator cookieGenerator = new CookieGenerator();
	
	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) arg0;
		HttpServletResponse response = (HttpServletResponse) arg1;
		String iscId = null;
		if (request.getCookies() != null) {
			for (Cookie cookie : request.getCookies()) {
				if (cookie.getName().toUpperCase().equals(RE_JSESSIONID)) {
					LOGGER.debug("Retrieve infinispan session cache id from cookie " + cookie.getValue());
					iscId = cookie.getValue();
				}
			}
		}
		if (iscId == null) {
			// Means first-time access
			cookieGenerator.setCookieName(RE_JSESSIONID);
			iscId = UUID.randomUUID().toString();
			LOGGER.debug("Add cookie value of infinispan session cache " + iscId);
			cookieGenerator.addCookie(response, iscId);
		}
		RE_JSESSION_ID_HOLDER.set(iscId);
		
		arg2.doFilter(arg0, arg1);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
