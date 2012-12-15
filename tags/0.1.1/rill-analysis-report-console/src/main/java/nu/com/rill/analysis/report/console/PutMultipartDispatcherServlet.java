package nu.com.rill.analysis.report.console;

import javax.servlet.http.HttpServletRequest;

import org.apache.commons.fileupload.FileUploadBase;
import org.springframework.beans.factory.NoSuchBeanDefinitionException;
import org.springframework.context.ApplicationContext;
import org.springframework.web.multipart.MultipartException;
import org.springframework.web.multipart.MultipartHttpServletRequest;
import org.springframework.web.multipart.MultipartResolver;
import org.springframework.web.servlet.DispatcherServlet;

public class PutMultipartDispatcherServlet extends DispatcherServlet {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;
	
	private MultipartResolver _multipartResolver;
	
	/**
	 * Initialize the MultipartResolver used by this class.
	 * <p>If no bean is defined with the given name in the BeanFactory for this namespace,
	 * no multipart handling is provided.
	 */
	private void initMultipartResolver(ApplicationContext context) {
		try {
			this._multipartResolver = context.getBean(MULTIPART_RESOLVER_BEAN_NAME, MultipartResolver.class);
			if (logger.isDebugEnabled()) {
				logger.debug("Using MultipartResolver [" + this._multipartResolver + "]");
			}
		}
		catch (NoSuchBeanDefinitionException ex) {
			// Default is no multipart resolver.
			this._multipartResolver = null;
			if (logger.isDebugEnabled()) {
				logger.debug("Unable to locate MultipartResolver with name '" + MULTIPART_RESOLVER_BEAN_NAME +
						"': no multipart request handling provided");
			}
		}
	}
	
	/**
	 * Initialize the strategy objects that this servlet uses.
	 * <p>May be overridden in subclasses in order to initialize further strategy objects.
	 */
	protected void initStrategies(ApplicationContext context) {
		
		super.initStrategies(context);
		
		initMultipartResolver(context);
	}
	
	/**
	 * Convert the request into a multipart request, and make multipart resolver available.
	 * <p>If no multipart resolver is set, simply use the existing request.
	 * @param request current HTTP request
	 * @return the processed request (multipart wrapper if necessary)
	 * @see MultipartResolver#resolveMultipart
	 */
	protected HttpServletRequest checkMultipart(HttpServletRequest request) throws MultipartException {
		
		HttpServletRequest superRequest = super.checkMultipart(request);
		if (superRequest instanceof MultipartHttpServletRequest) {
			logger.debug("Request is already a MultipartHttpServletRequest - if not in a forward, " +
					"this typically results from an additional MultipartFilter in web.xml");
		} else {
			String contentType = request.getContentType();
			if (contentType == null) {
	            return superRequest;
	        }
			if (contentType.toLowerCase().startsWith(FileUploadBase.MULTIPART)) {
				if (!"post".equals(request.getMethod().toLowerCase())) {
					return this._multipartResolver.resolveMultipart(superRequest);
		        }
	        }
		}
		
		return superRequest;
	}
}
