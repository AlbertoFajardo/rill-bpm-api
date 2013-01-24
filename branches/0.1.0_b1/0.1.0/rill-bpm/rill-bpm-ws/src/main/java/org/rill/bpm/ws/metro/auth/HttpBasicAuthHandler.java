package org.rill.bpm.ws.metro.auth;

import java.util.List;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.atomic.AtomicReference;

import javax.servlet.ServletContext;
import javax.xml.namespace.QName;
import javax.xml.ws.handler.MessageContext;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.springframework.beans.factory.config.ConfigurableBeanFactory;
import org.springframework.core.annotation.AnnotationUtils;
import org.springframework.util.CollectionUtils;
import org.springframework.util.ObjectUtils;
import org.springframework.util.StringUtils;
import org.springframework.web.context.support.WebApplicationContextUtils;

import com.sun.xml.ws.api.handler.MessageHandler;
import com.sun.xml.ws.api.handler.MessageHandlerContext;
import com.thoughtworks.xstream.core.util.Base64Encoder;

/**
 * HTTP BASIC AUTH handler.
 * @author mengran
 *
 */
@HttpBasicAuth(username="${metro.auth.username}", password="${metro.auth.password}")
public class HttpBasicAuthHandler implements MessageHandler<MessageHandlerContext> {
	
	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	private AtomicReference<ConfigurableBeanFactory> beanfactory = new AtomicReference<ConfigurableBeanFactory>();
	
	private HttpBasicAuth retrieveHttpBasicAuth(Class<?> implClass) {
		
		return AnnotationUtils.findAnnotation(implClass, HttpBasicAuth.class);
	}
	
	private String resolveHttpBasicAuthPlaceHolder(ConfigurableBeanFactory beanFactory, String placeHolder) {
		
		return beanFactory.resolveEmbeddedValue(placeHolder);
	}
	
	private String[] decodeAuthorization(List<String> authorizationList) {
		
		String authorization = CollectionUtils.isEmpty(authorizationList) ? null : authorizationList.get(0);
		authorization = StringUtils.hasLength(authorization) ? authorization : ":";
		authorization = authorization.replaceAll("Basic ", "");
    	Base64Encoder decoder = new Base64Encoder();
    	String afterDecode = new String(decoder.decode(authorization));
    	
    	return afterDecode.split(":");
	}
	
	private boolean doAuth(List<String> authorization, ConfigurableBeanFactory beanFactory) {
		
		boolean authResult = false;
		HttpBasicAuth httpBasicAuth = retrieveHttpBasicAuth(this.getClass());
		if (httpBasicAuth == null) {
			logger.debug("No http basic authentication config. return true");
			authResult = true;
		} else {
			String username = resolveHttpBasicAuthPlaceHolder(beanFactory, httpBasicAuth.username());
			String password = resolveHttpBasicAuthPlaceHolder(beanFactory, httpBasicAuth.password());
			
			String[] afterDecode = decodeAuthorization(authorization);
			
			authResult = afterDecode[0].equals(username) && afterDecode[1].equals(password);
			if (logger.isDebugEnabled()) {
				logger.debug("Http BASIC AUTH result: configuration- username " + username + " password " + password + ", -http header decode " + ObjectUtils.getDisplayString(afterDecode));
			}
		}
		
		return authResult;
	}

	@Override
	public boolean handleMessage(MessageHandlerContext context) {
		
		// Obtain bean factory for resolve embedded values
		beanfactory.compareAndSet(null, (ConfigurableBeanFactory) WebApplicationContextUtils.getRequiredWebApplicationContext((ServletContext) context.get(MessageContext.SERVLET_CONTEXT)).getAutowireCapableBeanFactory());
		
		@SuppressWarnings("unchecked")
		Map<String, List<String>> http_headers = (Map<String, List<String>>) context.get(MessageContext.HTTP_REQUEST_HEADERS);
        // retrieve Authorization http://www.ietf.org/rfc/rfc2617.txt
		List<String> authorization = http_headers.get("authorization");
		if (CollectionUtils.isEmpty(authorization)) {
			authorization = http_headers.get("Authorization");
		}
		if (CollectionUtils.isEmpty(authorization)) {
			authorization = http_headers.get("Proxy-Authorization");
		}
		
		return doAuth(authorization, beanfactory.get());
				
	}

	@Override
	public boolean handleFault(MessageHandlerContext context) {
		
		return true;
	}

	@Override
	public void close(MessageContext context) {
		
		// Do nothing
	}
	
	@Override
	public Set<QName> getHeaders() {

		return null;
	}

}
