package nu.com.rill.analysis.report;

import java.util.Map;

public interface ReportExportService {

	void export(String reportName, Map<String, String> mailParams, Map<String, String> reportParams) throws REException;
}
