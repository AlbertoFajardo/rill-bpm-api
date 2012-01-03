package org.rill.bpm.api.infinispan_d;

import javax.annotation.Resource;

import org.junit.Assert;
import org.junit.Test;
import org.rill.bpm.api.WorkflowCache;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;


@ContextConfiguration(value="classpath:org/rill/bpm/api/infinispan-d/infinispan.d.activiti.cfg.xml")
public class WorkflowCacheImplClusterTests extends AbstractJUnit4SpringContextTests {

	@Resource
	private WorkflowCache workflowCache;
	@Test
	public void getOrSetUserInfo() {
		
		String rillmeng = workflowCache.getOrSetUserInfo("what's your name", "cluster...");
		Assert.assertEquals("cluster...", rillmeng);
		
		String kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Michael Jackson");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
		kingOfPop = workflowCache.getOrSetUserInfo("King of pop", "Rill Meng");
		Assert.assertEquals("Michael Jackson", kingOfPop);
		
	}
}
