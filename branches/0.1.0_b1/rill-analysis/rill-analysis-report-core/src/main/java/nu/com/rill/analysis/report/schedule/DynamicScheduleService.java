package nu.com.rill.analysis.report.schedule;

import java.text.ParseException;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
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
	
	public void deleteJob(Runnable job) {
		
		Assert.notNull(job);
		
		JobDetail jd = createScheduleJob(job);
		try {
			if (getDynamicScheduler().getJobDetail(jd.getName(), jd.getGroup()) == null) {
				LOG.info("Do nothing because job not exists. " + job);
				return;
			}
			LOG.info("Delete job " + job);
			getDynamicScheduler().deleteJob(jd.getName(), jd.getGroup());
		} catch (SchedulerException e) {
			LOG.error("Do nothing because exception. " + e.getMessage(), e);
		}
		
	}
	
	private JobDetail createScheduleJob(Runnable job) {
		
		// 2. Create job
		MethodInvokingJobDetailFactoryBean jdb = new MethodInvokingJobDetailFactoryBean();
		jdb.setTargetObject(job);
		jdb.setTargetMethod("run");
		jdb.setName("job - " + job.toString());
		try {
			jdb.afterPropertiesSet();
		} catch (ClassNotFoundException e1) {
			throw new RuntimeException(e1);
		} catch (NoSuchMethodException e1) {
			throw new RuntimeException(e1);
		}
		
		return jdb.getObject();
	}
	
	public void submitJob(String cronExpression, Runnable job) {
		
		Assert.notNull(job);
		
		// 1. Validate report file
		boolean isValid = validate(cronExpression, job);
		if (!isValid) {
			return;
		}
				
		// 2. Create job
		JobDetail jd = createScheduleJob(job);
		
		// 3. Create trigger
		CronTriggerBean ctb = new CronTriggerBean();
		ctb.setName("trigger - " + job.toString());
		try {
			ctb.setCronExpression(cronExpression);
		} catch (ParseException e) {
			throw new RuntimeException(e);
		}
		ctb.setJobDetail(jd);
		try {
			ctb.afterPropertiesSet();
		} catch (Exception e1) {
			throw new RuntimeException(e1);
		}
		
		// 4. Register
		try {
			getDynamicScheduler().addJob(jd, true);
			getDynamicScheduler().scheduleJob(ctb);
		} catch (SchedulerException e) {
			throw new RuntimeException(e);
		}
		
		// 5. Persist
		persistJob(ctb);
		
	}
	
	protected boolean validate(String cronExpression, Runnable job) {
		
		Assert.hasText(cronExpression);
		
		JobDetail jd = createScheduleJob(job);
		try {
			if (getDynamicScheduler().getJobDetail(jd.getName(), jd.getGroup()) != null) {
				LOG.error("Job is exists. " + job);
				return false;
			}
		} catch (SchedulerException e) {
			LOG.error(e);
		}
		
		return true;
	}
	
	protected void persistJob(CronTriggerBean ctb) {
		
		// FIXME: MENGRAN.
	}

	@Override
	public void afterPropertiesSet() throws Exception {
		
		// FIXME: MENGRAN. Read jobs and publish schedule event
	}
	
	
}
