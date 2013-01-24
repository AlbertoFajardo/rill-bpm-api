package nu.com.rill.analysis.report.cluster;

import java.util.List;

import org.infinispan.remoting.transport.Address;
import org.infinispan.remoting.transport.Transport;
import org.infinispan.spring.provider.SpringEmbeddedCacheManager;
import org.junit.Assert;
import org.junit.BeforeClass;
import org.junit.Test;
import org.springframework.test.context.ContextConfiguration;
import org.springframework.test.context.junit4.AbstractJUnit4SpringContextTests;

@ContextConfiguration(value="classpath:conf/applicationContext-cache.xml")
public class SessionStickyFilterTests extends AbstractJUnit4SpringContextTests {

	private SpringEmbeddedCacheManager cacheManager;
	
	@BeforeClass
	public static void beforeClass() {
		
		System.setProperty("jgroups.tcpping.initial_hosts", "192.168.1.100[6800],192.168.1.100[6801]");
		System.setProperty("jgroups.tcp.port", "6800");
		if (System.getProperty("jgroups.tcpping.initial_hosts") != null && 
        		System.getProperty("jgroups.bind_addr") == null ) {
        	System.out.println("Add system property:jgroups.bind_addr=" + "192.168.1.100");
			System.setProperty("jgroups.bind_addr", "192.168.1.100");
        }
		
	}
	
	@Test
	public void testInfinispanAddress() {
		
		cacheManager = (SpringEmbeddedCacheManager) applicationContext.getBean("cacheManager");
		cacheManager.getCache("default");
		
		Transport transport = cacheManager.getNativeCacheManager().getTransport();
		Address address = transport.getAddress();
		Address physicalAddress = transport.getPhysicalAddresses().get(0);
		
		Assert.assertNotNull(address);
		System.out.println(address);
		System.out.println(physicalAddress);
		
		List<Address> members = cacheManager.getNativeCacheManager().getMembers();
		Assert.assertTrue(members.size() > 0);
	}

}
