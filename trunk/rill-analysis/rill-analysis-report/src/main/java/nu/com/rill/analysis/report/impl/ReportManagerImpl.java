package nu.com.rill.analysis.report.impl;

import java.io.ByteArrayInputStream;
import java.util.List;
import java.util.Map;

import nu.com.rill.analysis.report.ReportManager;
import nu.com.rill.analysis.report.bo.Report;
import nu.com.rill.analysis.report.dao.ReportDao;
import nu.com.rill.analysis.report.excel.ReportEngine;
import nu.com.rill.analysis.report.excel.ReportEngine.PARAM_CONFIG;
import nu.com.rill.analysis.report.schedule.DynamicScheduleService;

import org.springframework.util.Assert;

public class ReportManagerImpl implements ReportManager {
	
	private ReportDao reportDao;
	private DynamicScheduleService dynamicScheduleService;
	
	public final ReportDao getReportDao() {
		return reportDao;
	}

	public final void setReportDao(ReportDao reportDao) {
		this.reportDao = reportDao;
	}

	public final DynamicScheduleService getDynamicScheduleService() {
		return dynamicScheduleService;
	}

	public final void setDynamicScheduleService(
			DynamicScheduleService dynamicScheduleService) {
		this.dynamicScheduleService = dynamicScheduleService;
	}

	@Override
	public Report createReport(String name, String cronExpression, byte[] bytes) {
		
		Assert.notNull(name);
		Assert.notNull(bytes);
		
		Map<String, Map<PARAM_CONFIG, String>> params = ReportEngine.INSTANCE.retrieveReportParams(new ByteArrayInputStream(bytes), name);
		String reportParams = Report.serializeParams(params);
		
		Report reportDb = reportDao.insertReport(name, reportParams, cronExpression, bytes);
		
		return reportDb;
	}

	@Override
	public Report getReport(String name) {
		
		Assert.notNull(name);
		return reportDao.getReportByName(name);
	}
	
	@Override
	public List<Report> listReport() {
		
		return reportDao.findAllReportsExcludeContent();
	}

	@Override
	public Report updateReport(String name, byte[] bytes) {
		
		Map<String, Map<PARAM_CONFIG, String>> params = ReportEngine.INSTANCE.retrieveReportParams(new ByteArrayInputStream(bytes), name);
		String reportParams = Report.serializeParams(params);
		
		return reportDao.updateReport(name, reportParams, bytes);
	}

	@Override
	public void deleteReport(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public void scheduleReport(String name, String cronExpression, Runnable job) {
		
		Assert.hasText(name);
		Assert.hasText(cronExpression);
		
		reportDao.updateReport(name, cronExpression);
		dynamicScheduleService.submitJob(cronExpression, job);
	}

	@Override
	public void unscheduleReport(final String name) {
		
		reportDao.updateReport(name, (String) null);
		dynamicScheduleService.deleteJob(new Runnable() {
			
			@Override
			public String toString() {
				return name;
			}

			@Override
			public void run() {
				throw new UnsupportedOperationException();
			}
		});
	}

}
