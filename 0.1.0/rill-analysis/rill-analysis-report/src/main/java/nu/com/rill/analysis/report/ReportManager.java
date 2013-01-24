package nu.com.rill.analysis.report;

import java.util.List;

import nu.com.rill.analysis.report.bo.Report;

public interface ReportManager {

	Report createReport(String name, String cronExpression, byte[] bytes);
	
	Report getReport(String name);
	List<Report> listReport();
	
	Report updateReport(String name, byte[] bytes);
	
	void deleteReport(String name);
	
	void scheduleReport(String name, String cronExpression, Runnable job);
	
	void unscheduleReport(String name);
	
}
