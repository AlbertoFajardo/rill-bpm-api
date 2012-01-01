package org.rill.bpm.webclient.hotrod;

import org.infinispan.spring.provider.SpringRemoteCacheManager;
import org.junit.Test;
import org.springframework.beans.factory.annotation.Autowired;
import org.springframework.cache.Cache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(value="classpath:/conf/applicationContext-cache.xml")
public class RemoteCacheTests extends AbstractJUnit4SpringContextTests {

	@Autowired
	private SpringRemoteCacheManager cacheManager;
	
	@Test
	public void connectSuccess() {
		
		Cache cache = cacheManager.getCache("default");
		
	}

}
