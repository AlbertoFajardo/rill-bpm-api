package org.rill.bpm.ws.metro.hello;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.ws.metro.hello.service.HelloService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractTransactionalJUnit4SpringContextTests;

@ContextConfiguration(locations={"classpath:/conf/applicationContext-metro.xml"})
public class HelloServiceImplWithJTATest extends
		AbstractTransactionalJUnit4SpringContextTests {

	@Resource
	private HelloService helloService;
	
	public HelloService getHelloService() {
		return helloService;
	}

	public void setHelloService(HelloService helloService) {
		this.helloService = helloService;
	}

	@Test
	public void rillSayHello() {
		
		int count = getHelloService().whoSaid().size();
		logger.info("Who said count:" + count);
		
		getHelloService().sayHello("rill");
		Assert.assertTrue(getHelloService().whoSaid().size() == (count + 1));
	}
}
