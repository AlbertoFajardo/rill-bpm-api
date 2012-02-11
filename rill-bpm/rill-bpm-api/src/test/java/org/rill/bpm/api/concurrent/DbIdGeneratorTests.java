package org.rill.bpm.api.concurrent;

import java.util.List;

import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;

@ContextConfiguration(value="classpath:org/rill/bpm/api/concurrent/concurrent.activiti.cfg.xml")
public class DbIdGeneratorTests extends DbIdGeneratorTest {

	@SuppressWarnings("static-access")
	@Override
	@Test
	public void concurrentGetDbId() throws Throwable {
		
		try {
			System.out.println(Thread.currentThread().getName() + " sleep a moment.");
			Thread.currentThread().sleep(20000);
		} catch (InterruptedException e) {
			e.printStackTrace();
		}
		System.out.println(Thread.currentThread().getName() + " start working.");
		
		super.concurrentGetDbId();
	}

	@Override
	protected void processIdList(List<String> idList) {
		
		System.out.println("-----------------------------");
		for (String id : idList) {
			System.out.println(id);
		}
		System.out.println("-----------------------------");
		
	}

}
