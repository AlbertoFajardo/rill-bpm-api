package org.rill.bpm.api.support;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.api.WorkflowCache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration(value="classpath:org/rill/bpm/api/activiti.cfg.xml")
public class WorkflowCacheImplTest extends AbstractJUnit4SpringContextTests {

	@Resource
	private WorkflowCache workflowCache;
	@Test
	public void getOrSetUserInfo() {
		
		String rillmeng = workflowCache.getOrSetUserInfo("what's your name", "rillmeng");
		Assert.assertEquals("rillmeng", rillmeng);
		
		String rillmengFromCache = workflowCache.getOrSetUserInfo("what's your name", "Cache Invaild");
		Assert.assertEquals("rillmeng", rillmengFromCache);
		
		rillmengFromCache = workflowCache.getOrSetUserInfo("what's your name", "Cache Invaild - 2");
		Assert.assertEquals("rillmeng", rillmengFromCache);
	}
}
