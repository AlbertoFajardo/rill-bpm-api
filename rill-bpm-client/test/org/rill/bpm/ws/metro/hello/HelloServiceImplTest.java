package org.rill.bpm.ws.metro.hello;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.ws.metro.hello.service.HelloService;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(locations={"test-applicationContext-hello.xml"})
public class HelloServiceImplTest extends AbstractJUnit4SpringContextTests {

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
	
	@Test
	public void nullSayHello() {
		
		try {
			getHelloService().sayHello(null);
			Assert.fail("Should throw exception.");
		} catch (Exception e) {
			// Ignore except exception
		}
	}
	
	@Test
	public void transactionTest() {
		
		int count = getHelloService().whoSaid().size();
		logger.info("Who said count:" + count + ", means JDBC transaction manager take effect.");
		Assert.assertTrue(count == 1);
	}
}
