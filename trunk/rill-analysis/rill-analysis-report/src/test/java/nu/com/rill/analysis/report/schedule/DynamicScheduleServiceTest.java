package nu.com.rill.analysis.report.schedule;

import java.io.ByteArrayOutputStream;
import java.io.File;
import java.io.IOException;
import java.util.concurrent.atomic.AtomicInteger;

import nu.com.rill.analysis.report.excel.ReportEngine;

import org.apache.commons.io.FileUtils;
import org.junit.Assert;
import org.junit.Test;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.core.io.ClassPathResource;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;
import org.zkoss.poi.ss.usermodel.Workbook;

@ContextConfiguration(value="DynamicScheduleService.xml")
public class DynamicScheduleServiceTest extends AbstractJUnit4SpringContextTests {

	public static class NoValidate extends DynamicScheduleService {

		@Override
		protected boolean validate(JobDetail job, CronTrigger trigger) {
			
			// Do nothing
			return true;
		}
		
	}
	
	static class TestJob implements Runnable {
		
		static AtomicInteger ai = new AtomicInteger();
		
		private String reportFileName;
		public TestJob(String reportFileName) {
			this.reportFileName = reportFileName;
		}

		@Override
		public void run() {
			
			ReportEngine re = ReportEngine.INSTANCE;
			
			ClassPathResource cpr;
			try {
				cpr = new ClassPathResource("nu/com/rill/analysis/report/schedule/helloworld.xlsx");
				Workbook wb = re.generateReport(cpr.getInputStream(), "helloworld.xlsx", null);
				
				ByteArrayOutputStream baos = new ByteArrayOutputStream();
				wb.write(baos);
				File tmpImage = File.createTempFile("helloword_" + System.currentTimeMillis(), ".xlsx");
				FileUtils.writeByteArrayToFile(tmpImage, baos.toByteArray());
			} catch (IOException e) {
				throw new RuntimeException(e);
			}
			
			System.out.println("Run a report job :" + reportFileName);
			ai.incrementAndGet();
		}
		
	}
	
	@Autowired
	private DynamicScheduleService dynamicScheduleService;
	
	@Test
	public void submitReportJob() {
		
		for (int i = 0; i < 10 ; i++) {
			dynamicScheduleService.submitReportJob("0/2 * * * * ?", new TestJob("submitReportJob"));
		}
		
		try {
			Thread.sleep(5000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		
		Assert.assertTrue(TestJob.ai.get() > 0);
	}
}
