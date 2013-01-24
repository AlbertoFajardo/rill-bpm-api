/*
 * Copyright 2011-2012 the original author or authors.
 * 
 * Licensed under the Apache License, Version 2.0 (the "License");
 * you may not use this file except in compliance with the License.
 * You may obtain a copy of the License at
 * 
 *      http://www.apache.org/licenses/LICENSE-2.0
 * 
 * Unless required by applicable law or agreed to in writing, software
 * distributed under the License is distributed on an "AS IS" BASIS,
 * WITHOUT WARRANTIES OR CONDITIONS OF ANY KIND, either express or implied.
 * See the License for the specific language governing permissions and
 * limitations under the License.
 */
package org.springframework.data.hadoop.mapreduce;

import java.util.Collection;
import java.util.concurrent.CopyOnWriteArrayList;
import java.util.concurrent.CountDownLatch;
import java.util.concurrent.CyclicBarrier;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.apache.hadoop.mapred.JobClient;
import org.apache.hadoop.mapred.JobConf;
import org.apache.hadoop.mapred.RunningJob;
import org.apache.hadoop.mapreduce.Job;
import org.springframework.beans.factory.DisposableBean;
import org.springframework.beans.factory.FactoryBean;
import org.springframework.beans.factory.InitializingBean;
import org.springframework.util.Assert;

/**
 * Simple runner for submitting Hadoop jobs sequentially. By default, the runner waits for the jobs to finish and returns a boolean indicating
 * whether all the jobs succeeded or not (when there's no waiting, the status cannot be determined and null is returned).
 * <p/>
 * For more control over the job execution and outcome consider querying the {@link Job}s or using Spring Batch (see the reference documentation for more info). 
 * 
 * @author Costin Leau
 */
public class JobRunner implements FactoryBean<Object>, InitializingBean, DisposableBean {

	private static final Log log = LogFactory.getLog(JobRunner.class);

	private boolean runAtStartup = true;
	private boolean waitForJobs = true;
	private Collection<JobConf> jobs;
	private CopyOnWriteArrayList<RunningJob> runningJobs;
	private boolean executed = false;
//	private boolean succesful = false;

	private CyclicBarrier cb;
	
	@Override
	public void afterPropertiesSet() throws Exception {
		Assert.notEmpty(jobs, "at least one job needs to be specified");
		 
		runningJobs = new CopyOnWriteArrayList<RunningJob>();
		if (runAtStartup) {
			getObject();
		}
	}

	@Override
	public void destroy() throws Exception {
		if (!waitForJobs) {
			for (RunningJob runningJob : runningJobs) {
				try {
					log.info("Try to kill job " + runningJob.getID());
					runningJob.killJob();
				} catch (Exception ex) {
					log.warn("Cannot kill job [" + runningJob.getID() + "|" + runningJob.getJobName() + " ] failed", ex);
				}
			}
		}
	}
	
	private class JobWorker implements Runnable {
		
		private JobConf job;
		
		public JobWorker(JobConf job) {
			super();
			this.job = job;
		}

		@Override
		public void run() {
			
			log.info("Start to run job: " + job.getJobName());
			try {
				runningJobs.add(JobClient.runJob(this.job));
				cb.await();
			} catch (Exception e) {
				log.error(e);
				throw new RuntimeException(e);
			}
		}
		
	}
	@Override
	public Object getObject() throws Exception {
		if (!executed) {
			executed = true;
			
			final CountDownLatch cdl = new CountDownLatch(1);
			cb = new CyclicBarrier(jobs.size(), new Runnable() {

				@Override
				public void run() {
					log.info("All jobs have completion.");
					cdl.countDown();
				}
			});
			
			ExecutorService es =Executors.newFixedThreadPool(jobs.size());
			for (JobConf job : jobs) {
				es.submit(new JobWorker(job));
			}
			
			if (waitForJobs) {
				log.info("Waiting all jobs completion signal....");
				cdl.await();
			} else {
				log.info("No need wait any job completio, do continue.");
				cdl.countDown();
			}
		}
		
		return (waitForJobs ? true : null);
	}

	@Override
	public Class<?> getObjectType() {
		return Boolean.class;
	}

	@Override
	public boolean isSingleton() {
		return true;
	}


	/**
	 * Indicates whether the jobs should be submitted at startup or not.
	 * 
	 * @param runAtStartup The runAtStartup to set.
	 */
	public void setRunAtStartup(boolean runAtStartup) {
		this.runAtStartup = runAtStartup;
	}


	/**
	 * Indicates whether the runner should wait for the jobs to finish (the default) or not.
	 * 
	 * @param waitForJobs The waitForJobs to set.
	 */
	public void setWaitForJobs(boolean waitForJobs) {
		this.waitForJobs = waitForJobs;
	}

	/**
	 * Sets the Jobs to run.
	 * 
	 * @param jobs The jobs to run.
	 */
	public void setJobs(Collection<JobConf> jobs) {
		this.jobs = jobs;
	}
}