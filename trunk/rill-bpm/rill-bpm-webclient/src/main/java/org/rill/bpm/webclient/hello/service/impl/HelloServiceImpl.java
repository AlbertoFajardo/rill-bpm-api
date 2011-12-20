package org.rill.bpm.webclient.hello.service.impl;

import java.util.List;
import java.util.logging.Level;
import java.util.logging.Logger;

import org.rill.bpm.webclient.hello.dao.HelloDao;
import org.rill.bpm.webclient.hello.service.HelloService;
import org.springframework.transaction.annotation.Transactional;

public class HelloServiceImpl implements HelloService {

	protected final Logger logger = Logger.getLogger(getClass().getName());
	
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
			logger.log(Level.SEVERE, "Exception when try to persist who say hello.", e);
			throw new RuntimeException("Exception when try to persist who say hello.", e);
		}
	}

	@Override
	public List<String> whoSaid() {
		
		return getHelloDao().whoSaid();
	}

}
