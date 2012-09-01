package nu.com.rill.analysis.report.dao.impl;

import java.io.IOException;
import java.sql.PreparedStatement;
import java.sql.ResultSet;
import java.sql.SQLException;
import java.util.List;

import nu.com.rill.analysis.report.bo.Report;
import nu.com.rill.analysis.report.dao.ReportDao;

import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.simple.ParameterizedBeanPropertyRowMapper;
import org.springframework.jdbc.core.simple.ParameterizedSingleColumnRowMapper;
import org.springframework.jdbc.core.support.AbstractLobStreamingResultSetExtractor;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.jdbc.support.lob.DefaultLobHandler;
import org.springframework.jdbc.support.lob.LobHandler;
import org.springframework.util.Assert;

public class ReportDaoImpl extends JdbcDaoSupport implements ReportDao {

	@Override
	public Report insertReport(final String name, final String paramsXStrem, final String cronExpression,
			final byte[] content) {
		
		final Report newReport = getJdbcTemplate().execute("insert into report(name, paramsXStrem, cronExpression) values(?,?,?)", new PreparedStatementCallback<Report>() {

			@Override
			public Report doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				ps.setString(1, name);
				ps.setString(2, paramsXStrem);
				ps.setString(3, cronExpression);
				ps.execute();
				
				List<Integer> idList = getJdbcTemplate().query("select id from report where name = ?", new Object[] {name}, new ParameterizedSingleColumnRowMapper<Integer>());
				Assert.isTrue(idList.size() == 1, "report named " + name + " is not exists or have duplicated one.");
				
				Report r = new Report();
				r.setName(name);
				r.setCronExpression(cronExpression);
				r.setId(idList.get(0));
				r.setParamsXStrem(paramsXStrem);
				return r;
			}
		});
		
		getJdbcTemplate().execute("insert into report_byte(content, report_id) values(?,?)", new PreparedStatementCallback<Report>() {

			@Override
			public Report doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				ps.setBytes(1, content);
				ps.setInt(2, newReport.getId());
				ps.execute();
				
				newReport.setReportContent(content);
				return newReport;
			}
		});
		
		return newReport;
	}

	@Override
	public Report getReportById(Integer id) {
		
		List<Report> reportList = getJdbcTemplate().query("select * from report where id = ?", new Object[] {id}, new ParameterizedBeanPropertyRowMapper<Report>());
		Assert.isTrue(reportList.size() == 1, "report ided " + id + " is not exists or have duplicated one.");
		
		return getReportByName(reportList.get(0).getName());
	}

	@SuppressWarnings("unchecked")
	@Override
	public Report getReportByName(String name) {
		
		final List<Report> reportList = getJdbcTemplate().query("select * from report where name = ?", new Object[] {name}, ParameterizedBeanPropertyRowMapper.newInstance(Report.class));
		Assert.isTrue(reportList.size() == 1, "report named " + name + " is not exists or have duplicated one.");
		
		final LobHandler lobHandler = new DefaultLobHandler();
		getJdbcTemplate().query("select content from report_byte where report_id = ?", new Object[] {reportList.get(0).getId()}, new AbstractLobStreamingResultSetExtractor() {

			@Override
			protected void streamData(ResultSet rs) throws SQLException,
					IOException, DataAccessException {
				
				byte[] retrieveFromDb = lobHandler.getBlobAsBytes(rs, 1);
				reportList.get(0).setReportContent(retrieveFromDb);
			}
			
		});
		
		return reportList.get(0);
	}

	@Override
	public Report updateReport(String name, byte[] content) {
		
		return null;
	}

	@Override
	public Report updateReport(String name, final String cronExpression) {
		
		final Report reportDb = getReportByName(name);
		Assert.notNull(reportDb, "Can not find report named by " + name);
		
		getJdbcTemplate().execute("update report set cronExpression = ? where id = ?", new PreparedStatementCallback<Void>() {

			@Override
			public Void doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				ps.setString(1, cronExpression);
				ps.setInt(2, reportDb.getId());
				ps.execute();
				
				return null;
			}
		});
		
		reportDb.setCronExpression(cronExpression);
		
		return reportDb;
	}

	@Override
	public void deleteReport(String name) {
		// TODO Auto-generated method stub

	}

	@Override
	public List<Report> findAllReportsExcludeContent() {
		
		final List<Report> reportList = getJdbcTemplate().query("select * from report order by id asc", new Object[0], ParameterizedBeanPropertyRowMapper.newInstance(Report.class));
		return reportList;
	}

}
