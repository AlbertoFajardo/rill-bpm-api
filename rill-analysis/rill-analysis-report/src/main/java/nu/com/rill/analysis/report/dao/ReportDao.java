package nu.com.rill.analysis.report.dao;

import java.util.List;

import nu.com.rill.analysis.report.bo.Report;

public interface ReportDao {

	Report insertReport(String name, String paramsXStrem, String cronExpression, byte[] content);
	
	Report getReportById(Integer id);
	Report getReportByName(String name);
	
	List<Report> findAllReportsExcludeContent();
	
	Report updateReport(String name, byte[] content);
	Report updateReport(String name, String cronExpression);
	
	void deleteReport(String name);
}
