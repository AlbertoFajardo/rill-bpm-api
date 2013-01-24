package nu.com.rill.olap.datamart;

import java.sql.Connection;
import java.sql.PreparedStatement;
import java.sql.SQLException;
import java.sql.Timestamp;
import java.text.ParseException;
import java.text.SimpleDateFormat;
import java.util.Calendar;
import java.util.Date;
import java.util.Random;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.dao.DataAccessException;
import org.springframework.jdbc.core.JdbcTemplate;
import org.springframework.jdbc.core.PreparedStatementCallback;
import org.springframework.jdbc.core.PreparedStatementCreator;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(value="TFDataGeneratorTests.xml")
public class TFDataGeneratorTests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private JdbcTemplate transformedFunnelJdbcTemplate;
	
	private static final String START_TIME = "2010-01-01 00:00:00";
	
	@Test
	public void dim_winfo() {
		
		int existCnt = transformedFunnelJdbcTemplate.queryForInt("select count(*) from dim_winfo", new Object[0]);
		if (existCnt != 0) {
			logger.info("NO-OPs: winfo data has inserted");
			return;
		}
		
		logger.info("OPs: Start insert winfo data...");
		
		// Start insert
		transformedFunnelJdbcTemplate.execute(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				
				return con.prepareStatement("insert into dim_winfo(account, plan, unit, word) values(?,?,?,?)");
			}
			
		}, new PreparedStatementCallback<Void>() {

			@Override
			public Void doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				int index = 1;
				ps.setString(index, "张三的帐户");
				index++;
				ps.setString(index, "张三的计划A");
				index++;
				ps.setString(index, "张三的单元A");
				index++;
				ps.setString(index, "张三买的关键词A1");
				index++;
				ps.execute();
				
				index = 1;
				ps.setString(index, "张三的帐户");
				index++;
				ps.setString(index, "张三的计划A");
				index++;
				ps.setString(index, "张三的单元A");
				index++;
				ps.setString(index, "张三买的关键词A2");
				index++;
				ps.execute();
				
				index = 1;
				ps.setString(index, "张三的帐户");
				index++;
				ps.setString(index, "张三的计划A");
				index++;
				ps.setString(index, "张三的单元B");
				index++;
				ps.setString(index, "张三买的关键词B1");
				index++;
				ps.execute();
				
				index = 1;
				ps.setString(index, "李四的帐户");
				index++;
				ps.setString(index, "李四的计划A");
				index++;
				ps.setString(index, "李四的单元A");
				index++;
				ps.setString(index, "李四买的关键词A1");
				index++;
				ps.execute();
				
				index = 1;
				ps.setString(index, "李四的帐户");
				index++;
				ps.setString(index, "李四的计划C");
				index++;
				ps.setString(index, "李四的单元C");
				index++;
				ps.setString(index, "李四买的关键词C1");
				index++;
				ps.execute();
				
				return null;
			}
			
		});
		
		logger.info("OPs: Insert winfo data end.");
	}
	
	@Test
	public void dim_region() {
		
		int existCnt = transformedFunnelJdbcTemplate.queryForInt("select count(*) from dim_region", new Object[0]);
		if (existCnt != 0) {
			logger.info("NO-OPs: Region data has inserted");
			return;
		}
		
		logger.info("OPs: Start insert region data...");
		
		// Start insert
		transformedFunnelJdbcTemplate.execute(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				
				return con.prepareStatement("insert into dim_region(the_country, the_region, the_province, the_city) values(?,?,?,?)");
			}
			
		}, new PreparedStatementCallback<Void>() {

			@Override
			public Void doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				int index = 1;
				ps.setString(index, "中国");
				index++;
				ps.setString(index, "东北");
				index++;
				ps.setString(index, "辽宁");
				index++;
				ps.setString(index, "锦州");
				
				ps.execute();
				
				index = 1;
				ps.setString(index, "中国");
				index++;
				ps.setString(index, "东北");
				index++;
				ps.setString(index, "辽宁");
				index++;
				ps.setString(index, "沈阳");
				
				ps.execute();
				
				index = 1;
				ps.setString(index, "中国");
				index++;
				ps.setString(index, "华北");
				index++;
				ps.setString(index, "北京");
				index++;
				ps.setString(index, "朝阳");
				
				ps.execute();
				
				index = 1;
				ps.setString(index, "中国");
				index++;
				ps.setString(index, "华北");
				index++;
				ps.setString(index, "北京");
				index++;
				ps.setString(index, "海淀");
				
				ps.execute();
				
				index = 1;
				ps.setString(index, "中国");
				index++;
				ps.setString(index, "华北");
				index++;
				ps.setString(index, "河北");
				index++;
				ps.setString(index, "石家庄");
				
				ps.execute();
				
				return null;
			}
			
		});
		
		logger.info("OPs: Insert region data end.");
	}
	
	@Test
	public void dim_time_by_hour() throws Exception {
		
		for (Date date = new Date(); date.compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(START_TIME)) > 0; date = new Date(date.getTime() - 24 * 60 * 60 * 1000)) {
			Date now = date;
			final String dateStartStr = new SimpleDateFormat("yyyy-MM-dd 00:00:00").format(now);
			final String dateEndStr = new SimpleDateFormat("yyyy-MM-dd 23:59:59").format(now);
			
			String yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd").format(now);
			
			int existCnt = transformedFunnelJdbcTemplate.queryForInt("select count(*) from dim_time_by_hour where date_format(the_time, '%Y-%m-%d') = ?", yyyyMMdd);
			if (existCnt == 24) {
				logger.info("NO-OPs: Date: " + now + " has inserted");
				return;
			} else if (existCnt > 0 && existCnt < 24) {
				Assert.fail("Date: " + now + " has inserted, but not completed. " + existCnt);
			}
			
			// Start insert
			transformedFunnelJdbcTemplate.execute(new PreparedStatementCreator() {
				
				@Override
				public PreparedStatement createPreparedStatement(Connection con)
						throws SQLException {
					
					return con.prepareStatement("insert into dim_time_by_hour(the_time, the_year,the_quarter, the_month, the_week, the_day, the_ampm, the_hour) values(?,?,?,?,?,?,?,?)");
				}
				
			}, new PreparedStatementCallback<Void>() {

				@Override
				public Void doInPreparedStatement(PreparedStatement ps)
						throws SQLException, DataAccessException {
					
					try {
						for (Date d = new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateStartStr); 
								d.compareTo(new SimpleDateFormat("yyyy-MM-dd HH:mm:ss").parse(dateEndStr)) < 0;
								d = new Date(d.getTime() + 60 * 60 * 1000)) {
							
							String nowStr = new SimpleDateFormat("yyyy-MM-dd-H-mm-ss").format(d);
							int index = 1;
							ps.setTimestamp(index, new Timestamp(d.getTime()));
							index++;
							String yyyy = nowStr.split("-")[0];
							ps.setString(index, yyyy);
							index++;
							String q = "Q" + (new Integer(nowStr.split("-")[1])%3 == 0 ? new Integer(nowStr.split("-")[1])/3 : new Integer(nowStr.split("-")[1])/3 + 1);
							ps.setString(index, q);
							index++;
							ps.setString(index, yyyy + nowStr.split("-")[1]);
							// the_week
							Calendar c = Calendar.getInstance();
							c.setTime(d);
							int weekOfMonth = c.get(Calendar.DAY_OF_WEEK_IN_MONTH);
							index++;
							ps.setInt(index, weekOfMonth);
							index++;
							// the_day
							ps.setString(index, nowStr.split("-")[2]);
							index++;
							ps.setString(index, new Integer(nowStr.split("-")[3]) >= 12 ? "PM" : "AM");
							// the hour
							index++;
							ps.setString(index, new Integer(nowStr.split("-")[3]) + "点～" + (new Integer(nowStr.split("-")[3]) + 1) + "点");
							
							ps.execute();
							
						}
					} catch (NumberFormatException e) {
						throw new SQLException(e);
					} catch (ParseException e) {
						throw new SQLException(e);
					}
					
					return null;
				}
				
			});
		}
		
		logger.info("OPs: Insert time data end.");
		
	}
	
	@Test
	public void transformed_funnel_fact() {
		
		final int timeCnt = transformedFunnelJdbcTemplate.queryForInt("select count(*) from dim_time_by_hour", new Object[0]);
		final int winfoCnt = transformedFunnelJdbcTemplate.queryForInt("select count(*) from dim_winfo", new Object[0]);
		
		transformedFunnelJdbcTemplate.execute("delete from transformed_funnel_fact");
		logger.info("transformed_funnel_fact has been cleared.");
		
		// Start insert
		transformedFunnelJdbcTemplate.execute(new PreparedStatementCreator() {
			
			@Override
			public PreparedStatement createPreparedStatement(Connection con)
					throws SQLException {
				
				return con.prepareStatement("insert into transformed_funnel_fact(winfo_id, fk_account_id, fk_word_id" +
						", fk_time_id, fk_wmatch_id, fk_show_region_id, " +
						"show_cnt, click_cnt, access_cnt, ask_cnt, visitor_cnt, order_cnt, loaded_date) values(?, ?,?,?, ?,?,?, ?,?,?, ?,?,?)");
			}
			
		}, new PreparedStatementCallback<Void>() {

			@Override
			public Void doInPreparedStatement(PreparedStatement ps)
					throws SQLException, DataAccessException {
				
				String yyyyMMdd = new SimpleDateFormat("yyyy-MM-dd").format(new Date());
				
				for (int i = 0; i < timeCnt; i++) {
					// fk_winfo_id
					int index = 1;
					ps.setInt(index, new Random().nextInt(winfoCnt) + 1);
					index++;
					ps.setInt(index, 0);
					index++;
					ps.setInt(index, 0);
					// fk_time_id
					index++;
					ps.setInt(index, new Random().nextInt(timeCnt) + 1);
					index++;
					ps.setInt(index, 0);
					index++;
					// fk_show_region_id
					ps.setInt(index, new Random().nextInt(5) + 1);
					index++;
					
					// cnt start
					ps.setInt(index, new Random().nextInt(50000) + 50000);
					index++;
					ps.setInt(index, new Random().nextInt(3000) + 3000);
					index++;
					ps.setInt(index, new Random().nextInt(1000) + 2000);
					index++;
					ps.setInt(index, new Random().nextInt(500) + 300);
					index++;
					ps.setInt(index, new Random().nextInt(200) + 100);
					index++;
					ps.setInt(index, new Random().nextInt(30) + 1);
					index++;
					ps.setString(index, yyyyMMdd);
					index++;
					
					ps.execute();
				}
				
				return null;
			}
			
		});
		
		logger.info("OPs: Insert fact data end.");
	}

}
