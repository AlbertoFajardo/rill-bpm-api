package org.rill.bpm.web.sqltrace;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.glassfish.api.jdbc.SQLTraceListener;
import org.glassfish.api.jdbc.SQLTraceRecord;

public class SQLTraceListenerImpl implements SQLTraceListener {

	private static final Log LOGGER = LogFactory.getLog(SQLTraceListenerImpl.class);
	
	@Override
	public void sqlTrace(SQLTraceRecord record) {
		
		if (LOGGER.isDebugEnabled()) {
			LOGGER.debug(record);
		}
	}

}
