package nu.com.rill.analysis.report.excel.data;

import java.util.Map;

import nu.com.rill.analysis.report.excel.DataRetriever;

import org.zkoss.zss.model.Worksheet;

public class JsonDataRetriever implements DataRetriever {

	@Override
	public boolean supportType(DATA_TYPE dt) {
		
		return DATA_TYPE.json.equals(dt);
	}

	@Override
	public void retrieveData(Worksheet dataSheet, Map<String, String> reportParams) {
		
		return;
	}

}
