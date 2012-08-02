package nu.com.rill.analysis.report.schedule;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.quartz.CronTrigger;
import org.quartz.JobDetail;
import org.quartz.Scheduler;
import org.quartz.SchedulerException;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.scheduling.quartz.CronTriggerBean;
import org.springframework.scheduling.quartz.MethodInvokingJobDetailFactoryBean;
import org.springframework.util.Assert;

public class DynamicScheduleService implements InitializingBean {

	public final static Log LOG = LogFactory.getLog(DynamicScheduleService.class);
	
	private Scheduler dynamicScheduler;
	
	public final Scheduler getDynamicScheduler() {
		return dynamicScheduler;
	}

	public final void setDynamicScheduler(Scheduler dynamicScheduler) {
		this.dynamicScheduler = dynamicScheduler;
	}
	
	public void submitReportJob(String cronExpression, Runnable reportJob) {
		
		Assert.notNull(reportJob);
		Assert.notNull(cronExpression);
		
		// 2. Create job
		MethodInvokingJobDetailFactoryBean jdb = new MethodInvokingJobDetailFactoryBean();
		jdb.setTargetObject(reportJob);
		jdb.setTargetMethod("run");
		jdb.setName("job - " + reportJob.toString());
		try {
			jdb.afterPropertiesSet();
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException(e1);
		} catch (NoSuchMethodException e1) {
			throw new RuntimeException(e1);
		}
		
		// 3. Create trigger
		CronTriggerBean ctb = new CronTriggerBean();
		ctb.setName("trigger - " + reportJob.toString());
		try {
			ctb.setCronExpression(cronExpression);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		ctb.setJobDetail(jdb.getObject());
		try {
			ctb.afterPropertiesSet();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
				
		// 1. Validate report file
		boolean isValid = validate(jdb.getObject(), ctb);
		if (!isValid) {
			return;
		}
		
		// 4. Register
		try {
			getDynamicScheduler().addJob(jdb.getObject(), true);
			getDynamicScheduler().scheduleJob(ctb);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		
		// 5. Persist
		persistReportJob(ctb);
		
	}
	
	protected boolean validate(JobDetail job, CronTrigger trigger) {
		
		try {
			if (getDynamicScheduler().getJobDetail(job.getName(), job.getGroup()) != null) {
				// FIXME: MENGRAN. Need handle job exists case.
				return false;
			}
		} catch (SchedulerException e) {
			LOG.error("Can not get jobdetail. " + job, e);
		}
		
		return true;
	}
	
	protected void persistReportJob(CronTriggerBean ctb) {
		
		// FIXME: MENGRAN.
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		// Read jobs and register
		// FIXME: MENGRAN.
	}
	
	
}
