package org.rill.bpm.api.concurrent;

import java.util.ArrayList;
import java.util.LinkedHashSet;
import java.util.List;
import java.util.Set;
import java.util.concurrent.Callable;
import java.util.concurrent.ExecutorService;
import java.util.concurrent.Executors;
import java.util.concurrent.Future;

import javax.annotation.Resource;

import org.activiti.engine.impl.cfg.ProcessEngineConfigurationImpl;
import org.activiti.engine.impl.db.DbIdGenerator;
import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(value="classpath:org/rill/bpm/api/activiti.cfg.xml")
// @TestExecutionListeners({PeerMethodTestHelperTaskExecutionListener.class})
public class DbIdGeneratorTest extends AbstractJUnit4SpringContextTests {
	
	private static final int CONCURRENT_SIZE = 5000;
	private static final int THREADPOOL_SIZE = 100;
	@Resource
	private ProcessEngineConfigurationImpl processEngineConfiguration;
	
	private class MyDbIdGenerator extends DbIdGenerator {

		public MyDbIdGenerator(DbIdGenerator delegate) {
			super();
			setCommandExecutor(delegate.getCommandExecutor());
			setIdBlockSize(delegate.getIdBlockSize());
		}

		@Override
		protected synchronized void getNewBlock() {
			System.out.println(Thread.currentThread().getName() + " fetch new block from db.");
			super.getNewBlock();
		}
	}
	
	protected void logNextId (String nextId) {
		System.out.println(Thread.currentThread().getName() + " get next ID - " + nextId);
	}
	
	@Test
	public void concurrentGetDbId() throws Throwable {
		
		final DbIdGenerator idGenerator = (DbIdGenerator) processEngineConfiguration.getIdGenerator();
		final MyDbIdGenerator myIdGenerator = new MyDbIdGenerator(idGenerator);
		
		ExecutorService threadPool = Executors.newFixedThreadPool(THREADPOOL_SIZE);
		Callable<String> getId = new Callable<String>() {

			@Override
			public String call() throws Exception {
				
				String nextId = "error";
				try {
					nextId = myIdGenerator.getNextId();
				} catch (Exception e) {
					System.out.println(Thread.currentThread().getName() + " get next ID - " + nextId);
					e.printStackTrace();
//					throw e;
				}
				logNextId(nextId);
				return nextId;
			}
		};
		List<Callable<String>> getIdTasks = new ArrayList<Callable<String>>(CONCURRENT_SIZE);
		for (int i = 0; i < CONCURRENT_SIZE; i++) {
			getIdTasks.add(getId);
		}
		
		List<String> getIdList = new ArrayList<String>(CONCURRENT_SIZE);
		Set<String> getIdSet = new LinkedHashSet<String>(CONCURRENT_SIZE);
		try {
			List<Future<String>> futures = threadPool.invokeAll(getIdTasks);
			Assert.assertTrue(futures.size() == CONCURRENT_SIZE);
			for (Future<String> f : futures) {
				getIdList.add(f.get());
				boolean addFlag = getIdSet.add(f.get());
				if (!"error".equals(f.get())) {
					Assert.assertTrue("Add f.get() " + f.get(), addFlag);
				}
			}
//			Assert.assertTrue(getIdSet.size() == CONCURRENT_SIZE);
			Assert.assertTrue(getIdList.size() == CONCURRENT_SIZE);
		} finally {
			processIdList(getIdList);
		}
		
	}
	
	protected void processIdList(List<String> idList) {
		
	}

}
