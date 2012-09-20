package nu.com.rill.analysis.report.cluster;

import java.io.IOException;

import javax.servlet.Filter;
import javax.servlet.FilterChain;
import javax.servlet.FilterConfig;
import javax.servlet.ServletContext;
import javax.servlet.ServletException;
import javax.servlet.ServletRequest;
import javax.servlet.ServletResponse;
import javax.servlet.http.Cookie;
import javax.servlet.http.HttpServletRequest;
import javax.servlet.http.HttpSession;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.springframework.cache.Cache.ValueWrapper;
import org.springframework.web.context.support.WebApplicationContextUtils;
import org.zkoss.zk.ui.Session;
import org.zkoss.zk.ui.WebApp;
import org.zkoss.zk.ui.http.SimpleSessionCache;
import org.zkoss.zk.ui.sys.SessionCache;

public class InfinispanSessionCache extends SimpleSessionCache implements SessionCache, Filter {

	private static final Log LOGGER = LogFactory.getLog(InfinispanSessionCache.class);
	public static final String CACHE_NAME = "default";
	
	private static final ThreadLocal<String> REQUEST_JSESSION_ID = new ThreadLocal<String>();
	
	private SpringEmbeddedCacheManager cacheManager;

	@Override
	public void init(WebApp wapp) {
		
		cacheManager = (SpringEmbeddedCacheManager) WebApplicationContextUtils.getRequiredWebApplicationContext(
					(ServletContext) wapp.getNativeContext()).getBean("cacheManager");
	}

	@Override
	public void put(Session sess) {
		
		super.put(sess);
		String sessionId = ((HttpSession) sess.getNativeSession()).getId();
		
		LOGGER.debug("Put session into cache using key " + sessionId);
		cacheManager.getCache(CACHE_NAME).put(sessionId, sess);
		
	}

	@Override
	public Session get(Object navsess) {
		
		Session s = super.get(navsess);
		if (s != null) {
			return s;
		}
		
		String sessionId = ((HttpSession) navsess).getId();
		if (REQUEST_JSESSION_ID.get() != null) {
			sessionId = REQUEST_JSESSION_ID.get();
			LOGGER.debug("Use session key " + sessionId + " to find session in cache.");
		}
		ValueWrapper vw = cacheManager.getCache(CACHE_NAME).get(sessionId);
		return vw == null ? null : (Session) vw.get();
	}

	@Override
	public void destroy() {
		
	}

	@Override
	public void doFilter(ServletRequest arg0, ServletResponse arg1,
			FilterChain arg2) throws IOException, ServletException {
		
		HttpServletRequest request = (HttpServletRequest) arg0;
		for (Cookie cookie : request.getCookies()) {
			if (cookie.getName().toUpperCase().equals("JSESSIONID")) {
				LOGGER.debug("Put request JSESSIONID into threadlocal" + cookie.getValue());
				REQUEST_JSESSION_ID.set(cookie.getValue());
			}
		}
		
		arg2.doFilter(arg0, arg1);
	}

	@Override
	public void init(FilterConfig arg0) throws ServletException {
		
	}

}
