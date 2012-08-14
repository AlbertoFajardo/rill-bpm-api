package nu.com.rill.analysis.report.excel;

import java.util.Map;

import org.zkoss.zss.model.Worksheet;

public interface DataRetriever {

	public enum DATA_TYPE {
		json, mdx
	}
	
	boolean supportType(DATA_TYPE dt);
	
	void retrieveData(Worksheet dataSheet, Map<String, String> reportParams);
	
}
