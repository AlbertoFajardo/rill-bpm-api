package nu.com.rill.analysis.report;

import org.springframework.util.ObjectUtils;
import org.zkoss.lang.Expectable;


public class REException extends RuntimeException implements Expectable {

	/**
	 * 
	 */
	private static final long serialVersionUID = 1L;

	public REException() {
		super();
	}

	public REException(String message, Throwable cause) {
		super(message, cause);
	}

	public REException(String message) {
		super(message);
	}

	public REException(Throwable cause) {
		super(cause);
	}

	@Override
	public String getMessage() {
		
		StringBuilder sb = new StringBuilder().append(super.getMessage()).append("\n");
		Throwable caused = this.getCause();
		while (caused != null && caused instanceof REException) {
			sb.append(ObjectUtils.getDisplayString(caused.getMessage())).append("\n");
			caused = caused.getCause();
		}
		return sb.toString();
	}
	
}
