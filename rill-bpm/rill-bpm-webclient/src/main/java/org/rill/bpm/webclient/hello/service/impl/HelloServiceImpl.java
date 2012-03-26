package org.rill.bpm.webclient.hello.service.impl;

import java.util.List;

import org.apache.commons.logging.Log;
import org.apache.commons.logging.LogFactory;
import org.rill.bpm.webclient.hello.dao.HelloDao;
import org.rill.bpm.webclient.hello.service.HelloService;
import org.springframework.transaction.annotation.Transactional;
import org.springframework.util.Assert;

public class HelloServiceImpl implements HelloService {

	protected final Log logger = LogFactory.getLog(getClass().getName());
	
	private HelloDao helloDao;
	
	public HelloDao getHelloDao() {
		return helloDao;
	}

	public void setHelloDao(HelloDao helloDao) {
		this.helloDao = helloDao;
	}

	@Override
	@Transactional
	public void sayHello(String name) {

		try {
			getHelloDao().createHello(name);
		} catch (Exception e) {
			logger.error("Exception when try to persist who say hello.", e);
			throw new RuntimeException("Exception when try to persist who say hello.", e);
		}
	}
	
	@Override
	@Transactional
	public void sayHello(String name, int cnt) {
		
		Assert.isTrue(cnt > 1, "Invalid say hello cnt.");
		for (int i = 0; i < cnt; i++) {
			this.sayHello(name + "_" + i);
		}
	}

	@Override
	public List<String> whoSaid() {
		
		return getHelloDao().whoSaid();
	}

	@Override
	@Transactional
	public void batchSayHello(String[] names) {
		
		Assert.notEmpty(names);
		
		for (String name : names) {
			logger.info("Batch say hello: " + name);
			this.sayHello(name);
		}
	}

	@Override
	@Transactional
	public void deleteSayHello(String name) {
		
		List<String> whoSaid = whoSaid();
		for (String who : whoSaid) {
			if (who.startsWith(name)) {
				logger.info("delete " + who);
				getHelloDao().deleteHello(who);
				break;
			}
		}
		
	}

}
