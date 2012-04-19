package org.rill.bpm.webclient.hello.dao.impl;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Statement;
import java.util.List;

import org.rill.bpm.webclient.hello.dao.HelloDao;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.BadSqlGrammarException;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.jdbc.core.SingleColumnRowMapper;
import org.springframework.jdbc.core.StatementCallback;
import org.springframework.jdbc.core.support.JdbcDaoSupport;
import org.springframework.util.Assert;
import org.springframework.util.StringUtils;

public class HelloDaoImpl extends JdbcDaoSupport implements HelloDao {

	public static final String CREATE_TABLE_HELLO = "create table hello (id int(10) NOT NULL auto_increment, whosay varchar(100) NOT NULL, primary key (id));";
	
	@Override
	protected void initTemplateConfig() {
		
		try {
			getJdbcTemplate().queryForInt("select count(id) from hello");
		} catch (BadSqlGrammarException bsge) {
			logger.warn("Maybe need create hello table first.", bsge);
			getJdbcTemplate().execute(new StatementCallback<Void>() {

				@Override
				public Void doInStatement(Statement stmt)
						throws SQLException, DataAccessException {
					// Do create table
					stmt.executeUpdate(CREATE_TABLE_HELLO);
					return null;
				}
			});
			// No throw exception means create successfully.
			getJdbcTemplate().queryForInt("select count(id) from hello");
		}
		
		logger.info("Schema check Pass.");
	}

	@Override
	public void createHello(String name) {
		
		Assert.isTrue(StringUtils.hasText(name), "Who say hello? Empty is not permitted.");
		Assert.isTrue(!name.toLowerCase().contains("rollback"), "Yes sir, we rollback it.");
		
		final String nameAfterTrim = StringUtils.trimWhitespace(name);
		
		int cnt = getJdbcTemplate().execute(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				
				return con.prepareStatement("insert into hello(whosay) values('" + nameAfterTrim + "')");
			}
		}, new PreparedStatementCallback<Integer>() {

			@Override
			public Integer doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				logger.info(nameAfterTrim + " say hello to us.");
				return ps.executeUpdate();
			}
		});
		
		Assert.isTrue(cnt == 1, "Insert failed. SQL return:" + cnt);
		
	}

	@Override
	public List<String> whoSaid() {
		
		return getJdbcTemplate().query("select whosay from hello", new SingleColumnRowMapper<String>());
	}

	@Override
	public void deleteHello(String name) {
		
		Assert.isTrue(StringUtils.hasText(name), "Delete who? Empty is not permitted.");
		
		final String nameAfterTrim = StringUtils.trimWhitespace(name);
		
		int cnt = getJdbcTemplate().execute(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				
				return con.prepareStatement("update hello set whosay = 'deleted " + nameAfterTrim + "' where whosay ='" + nameAfterTrim + "'");
			}
		}, new PreparedStatementCallback<Integer>() {

			@Override
			public Integer doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				logger.info(nameAfterTrim + " has marked deleted.");
				return ps.executeUpdate();
			}
		});
		
		Assert.isTrue(cnt == 1, "Update failed. SQL return:" + cnt);
		
	}

}
