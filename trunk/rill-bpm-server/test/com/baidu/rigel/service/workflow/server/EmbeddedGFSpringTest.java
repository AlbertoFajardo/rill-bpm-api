package com.baidu.rigel.service.workflow.server;

import org.junit.Assert;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

/**
 * Test embedded-GF & Spring integration test.
 * 
 * @author mengran
 *
 */
@ContextConfiguration(value={"classpath:/conf/applicationContext-embeddedgf.xml"})
public class EmbeddedGFSpringTest extends AbstractJUnit4SpringContextTests {

	@Test
	public void embeddedGFInSpring() {
		
		// No exception means OK.
		Assert.assertTrue(true);
	}
}
